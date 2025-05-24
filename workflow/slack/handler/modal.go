package handler

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"

	obhttp "workflow/slack-app/http"
	"workflow/slack-app/message"

	"workflow/slack-app/base"

	"github.com/jackc/pgx/v4"
	"github.com/slack-go/slack"
)

type ModalController struct {
	obieContext *base.ObieContext
}

func NewModalController(context *base.ObieContext) *ModalController {
	return &ModalController{
		obieContext: context,
	}
}

func (m ModalController) Modal(w http.ResponseWriter, r *http.Request) {
	log := m.obieContext.GetLog()

	// check if incoming request is valid
	err := verifySigningSecret(m, r)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	// check if incoming request is valid
	formValueByteArray := []byte(r.FormValue("payload"))
	payload, err := getPayload(formValueByteArray)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// do not process any block actions other than submit event
	if payload["type"] == "block_actions" {
		return
	}

	if payload["token"] != os.Getenv("SLACK_VERIFICATION_TOKEN") {
		log.Error("SLACK_VERIFICATION_TOKEN invalid")
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	// get current channel from private_metadata
	var curchannel string
	if payload["view"] != nil && payload["view"].(map[string]interface{})["private_metadata"] != nil {
		curchannel = payload["view"].(map[string]interface{})["private_metadata"].(string)
	}

	api := slack.New(os.Getenv("SLACK_BOT_TOKEN"))
	var currentModal string
	if payload["view"] != nil && payload["view"].(map[string]interface{})["title"] != nil && payload["view"].(map[string]interface{})["title"].(map[string]interface{})["text"] != nil {
		currentModal = payload["view"].(map[string]interface{})["title"].(map[string]interface{})["text"].(string)
	}

	switch currentModal {
	case "Configure":
		{
			processConfigureModal(m, w, payload, curchannel, api)
		}
	case "AskOB":
		{
			processNewIndidentModal(m, w, payload, curchannel, api)
		}
	default:
		{
			if err != nil {
				log.Error(err.Error())
				w.WriteHeader(http.StatusInternalServerError)
				return
			}
		}
	}

}

// Method to upsert `origin` and `to` channels
func processConfigureModal(m ModalController, w http.ResponseWriter, payload map[string]interface{}, channel string, api *slack.Client) {
	db := m.obieContext.GetDB()
	log := m.obieContext.GetLog()

	var toChannel string
	user := payload["user"].(map[string]interface{})["id"].(string)
	userName, err := getInfoHelper("User", user, api)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// to channel set to current channel by default
	for _, val := range payload["view"].(map[string]interface{})["state"].(map[string]interface{})["values"].(map[string]interface{}) {
		if val.(map[string]interface{})["select_to_channel"] != nil {
			toChannel = val.(map[string]interface{})["select_to_channel"].(map[string]interface{})["selected_conversation"].(string)
		}
	}

	channelName, err := getInfoHelper("Channel", channel, api)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	toChannelName, err := getInfoHelper("Channel", toChannel, api)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	teamID := payload["view"].(map[string]interface{})["team_id"].(string)

	// --------------------------------------------------------------------------------------
	// get the primary key for the current workspace

	var teamIDPK int
	err = db.QueryRow(context.Background(), "SELECT id FROM analytics.askob_workspace WHERE key=$1", teamID).Scan(&teamIDPK)
	if err != nil {
		log.Error(os.Stderr, "QueryRow failed: %v\n", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// --------------------------------------------------------------------------------------
	// store configuration details in db upsert
	var routingID int
	err = db.QueryRow(context.Background(), "SELECT id FROM analytics.askob_routing WHERE channel_origin=$1", channel).Scan(&routingID)
	if err != nil {
		if err == pgx.ErrNoRows {
			log.Info("Inserting new configuration...")
			insertNewConfiguration(channel, toChannel, teamIDPK, teamIDPK, m, w)
		} else {
			log.Error(os.Stderr, "QueryRow failed: %v\n", err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
	} else {
		log.Info("found a record..updating configuration")
		updateExistingConfiguration(routingID, channel, toChannel, teamIDPK, teamIDPK, m, w)
	}

	log.Info("Configuration upserted in DB.")

	// let admin know that he has configured `from` and `to` channels in his current working channel
	msg := fmt.Sprintf("Hi %s. You decided to send from #%s to #%s.", userName, channelName, toChannelName)
	_, err = api.PostEphemeral(
		channel,
		user,
		slack.MsgOptionText(msg, false),
		slack.MsgOptionAttachments())

	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
}

// Method to update existing configuration
func updateExistingConfiguration(routingID int, fromChannel string, toChannel string, originWorkspaceID int, toWorkspaceID int, m ModalController, w http.ResponseWriter) {

	log := m.obieContext.GetLog()
	updateRoutingUrl := os.Getenv("CONNECT_BASE_URL") + "routing"
	// update data in askob_routing table
	data := make(map[string]interface{})
	data["id"] = routingID
	data["channel_origin"] = fromChannel
	data["channel_to"] = toChannel
	data["origin_workspace_id"] = originWorkspaceID
	data["to_workspace_id"] = toWorkspaceID

	updateRoutingResult, err := obhttp.MakeHttpCall(log, "PUT", updateRoutingUrl, data)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	if (updateRoutingResult["responseCode"] != float64(200)) && (updateRoutingResult["responseCode"] != float64(201)) {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
}

// Method to insert new configuraion
func insertNewConfiguration(fromChannel string, toChannel string, originWorkspaceID int, toWorkspaceID int, m ModalController, w http.ResponseWriter) {
	log := m.obieContext.GetLog()
	insertRoutingUrl := os.Getenv("CONNECT_BASE_URL") + "routing"

	// store data in askob_routing table
	data := make(map[string]interface{})
	data["channel_origin"] = fromChannel
	data["channel_to"] = toChannel
	data["origin_workspace_id"] = originWorkspaceID
	data["to_workspace_id"] = toWorkspaceID

	insertRoutingResult, err := obhttp.MakeHttpCall(log, "POST", insertRoutingUrl, data)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	if (insertRoutingResult["responseCode"] != float64(200)) && (insertRoutingResult["responseCode"] != float64(201)) {
		w.WriteHeader(http.StatusConflict)
		return
	}
}

// Helper method to get Payload from slack
func getPayload(inputForm []byte) (map[string]interface{}, error) {
	var incident slack.InteractionCallback
	err := json.Unmarshal(inputForm, &incident)
	marshalIncident, _ := json.Marshal(incident)
	var payload map[string]interface{}
	err = json.Unmarshal(marshalIncident, &payload)
	return payload, err
}

// Helper method to verify secrets from slack
func verifySigningSecret(m ModalController, r *http.Request) error {
	log := m.obieContext.GetLog()
	verifier, err := slack.NewSecretsVerifier(r.Header, os.Getenv("SIGN_IN_SECRET"))
	if err != nil {
		log.Error(err.Error())
		return err
	}

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		log.Error(err.Error())
		return err
	}
	// Need to use r.Body again when unmarshalling SlashCommand and InteractionCallback
	r.Body = ioutil.NopCloser(bytes.NewBuffer(body))

	verifier.Write(body)
	if err = verifier.Ensure(); err != nil {
		log.Error(err.Error())
		return err
	}
	return nil
}

// Helper method to process new incidents raised by customers
func processNewIndidentModal(m ModalController, w http.ResponseWriter, payload map[string]interface{}, fromChannel string, api *slack.Client) {
	log := m.obieContext.GetLog()

	user := payload["user"].(map[string]interface{})["id"].(string)
	userName, err := getInfoHelper("User", user, api)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	//  get ticket description and severity
	var respSummary, respSeverity string
	for key, val := range payload["view"].(map[string]interface{})["state"].(map[string]interface{})["values"].(map[string]interface{}) {
		if key != "Summary" {
			respSeverity = val.(map[string]interface{})["severity"].(map[string]interface{})["selected_option"].(map[string]interface{})["value"].(string)
		} else {
			respSummary = val.(map[string]interface{})["summary"].(map[string]interface{})["value"].(string)
		}
	}

	// go routine to create new incident and update ticket table
	go func() {
		data2 := make(map[string]interface{})
		data2["title"] = "New Incident created"
		data2["description"] = respSummary
		data2["assigned_to"] = ""
		data2["status"] = StatusNew
		data2["created_by"] = userName
		data2["updated_by"] = userName
		data2["severity"] = respSeverity //typo

		// insert to ticket table to get uuid
		insertToTicketTableResult := message.TicketingInsert("POST", w, "", data2, m.obieContext.GetLog())
		
		if (insertToTicketTableResult["responseCode"] != float64(200)) && (insertToTicketTableResult["responseCode"] != float64(201)) {
			w.WriteHeader(http.StatusInternalServerError)
			return
		}

		ticketNumber := insertToTicketTableResult["entity"].(map[string]interface{})["canonical_id"].(string)
		// update slack channel about new ticket creation
		respTimestamp, err := message.PostTicketMessage(respSeverity, respSummary, userName, api, fromChannel, ticketNumber, "", "", StatusNew)
		if err != nil {
			log.Error(err.Error())
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		
		// insert to message table for the ticket created
		insertMessageResult, err := message.InsertToMessageTable(log, user, respTimestamp)
		if err != nil {
			log.Error(err.Error())
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		if (insertMessageResult["responseCode"] != float64(200)) && (insertMessageResult["responseCode"] != float64(201)) {
			return
		}		

		data2["id"] = insertToTicketTableResult["entity"].(map[string]interface{})["id"].(float64)
		data2["askob_message_id"] = insertMessageResult["entity"].(map[string]interface{})["id"].(float64)
		data2["canonical_id"] = ticketNumber
				
		// update ticket table with the message fk from previous step
		message.TicketingInsert("PUT", w, respTimestamp, data2, m.obieContext.GetLog())
	}()

	return
}
