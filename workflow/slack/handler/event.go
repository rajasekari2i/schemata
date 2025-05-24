package handler

import (
	"context"
	"encoding/json"	
	"io/ioutil"
	"net/http"
	"os"
	"strings"

	"workflow/slack-app/base"
	"workflow/slack-app/message"

	"github.com/jackc/pgx/v4"
	"github.com/slack-go/slack"
)

const (
	SevLow    = "LOW"
	SevMedium = "MEDIUM"
	SevHigh   = "HIGH"

	StatusNew    = "NEW"
	StatusOpen   = "OPEN"
	StatusSolved = "SOLVED"
)

type EventController struct {
	obieContext *base.ObieContext
}

func NewEventController(context *base.ObieContext) *EventController {
	return &EventController{
		obieContext: context,
	}
}

func (e EventController) Event(w http.ResponseWriter, r *http.Request) {
	log := e.obieContext.GetLog()

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	//unstructured json includes both oauth pass and fail case
	var event map[string]interface{}
	json.Unmarshal(body, &event)

	if event["token"] != os.Getenv("SLACK_VERIFICATION_TOKEN") {
		log.Error("event[token] does not match SLACK_VERIFICATION_TOKEN")
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	switch event["type"] {
	case "url_verification":
		{
			handleURLVerification(event, w)
		}
	case "event_callback":
		{
			handleUserEvent(event, e, w)
		}
	default:
		{
			log.Error(err.Error())
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
	}
}

// Helper method to validate event endpoint
func handleURLVerification(event map[string]interface{}, w http.ResponseWriter) {
	w.WriteHeader(http.StatusOK)
	w.Header().Set("Content-Type", "application/text")
	w.Write([]byte(event["challenge"].(string)))
	return
}

type preCheckData struct {
	fromChannel, toChannel                      string
	routingID, originWorkspaceID, toWorkspaceID int
	toChannelCheck                              bool
}

// Method to handle user emoji clicks
func handleUserEvent(event map[string]interface{}, e EventController, w http.ResponseWriter) {
	db := e.obieContext.GetDB()
	log := e.obieContext.GetLog()

	assignedTo := event["event"].(map[string]interface{})["user"].(string)
	emoji := event["event"].(map[string]interface{})["reaction"].(string)
	ts := event["event"].(map[string]interface{})["item"].(map[string]interface{})["ts"].(string)
	curchannelID := event["event"].(map[string]interface{})["item"].(map[string]interface{})["channel"].(string)

	// pre-check logic: if current channel is not `from` or `to`, send error
	preCheckVal, err := preChecks(e, curchannelID, w)
	if err != nil {
		log.Error(os.Stderr, "precheck to handle user events failed: %v", err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	api := slack.New(os.Getenv("SLACK_BOT_TOKEN"))
	assignedToName, err := getInfoHelper("User", assignedTo, api)
	if err != nil {
		log.Error("%s\n", err)
		return
	}

	params := slack.GetConversationRepliesParameters{
		ChannelID: curchannelID,
		Timestamp: ts,
	}

	msgs, _, _, err := api.GetConversationReplies(&params)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	var jsonMsg map[string]interface{}
	var msgSlice []string
	var createdBy string
	if len(msgs) == 1 {
		marshalMsgs, _ := json.Marshal(msgs[0])
		json.Unmarshal(marshalMsgs, &jsonMsg)

		createdBy, err = getInfoHelper("User", jsonMsg["user"].(string), api)
		if err != nil {
			log.Error("%s\n", err)
			return
		}

		// 2 cases to handle
		// 1. slash command ex: /obie problem stmt
		// 2. non-slash regular texts ex: problem stmt

		if jsonMsg["text"] != nil { // non-slash command messages (regular text inputs )
			msgSlice = append(msgSlice, jsonMsg["text"].(string))
			if jsonMsg["thread_ts"] == nil {
				// emoji clicked on main msg to create ticket
				doParentMessageOperations(e, w, api, emoji, ts, curchannelID, preCheckVal, assignedTo, msgSlice, createdBy, SevMedium)
			} else {
				// emoji clicked on thread msg to talk between threads
				if emoji == "white_circle" {
					talkWithinSlackThreads(e, w, jsonMsg, api)
				}
			}
		} else { // slash command messages. ticket already created as attachment. so message is nil

			// no-op if eyes clicked on to channel.
			if preCheckVal.toChannelCheck && emoji == "eyes" {
				log.Info("no-op reacted with eyes on to channel")
				w.WriteHeader(http.StatusOK)
				return
			}

			var respSeverity, userName string
			for _, val := range jsonMsg["attachments"].([]interface{})[0].(map[string]interface{})["fields"].([]interface{}) {
				if val.(map[string]interface{})["title"].(string) == "Priority:" {
					respSeverity = val.(map[string]interface{})["value"].(string)
				} else if val.(map[string]interface{})["title"].(string) == "Created By:" {
					userName = val.(map[string]interface{})["value"].(string)
				}
			}

			//send message to `to` chananel
			text := jsonMsg["attachments"].([]interface{})[0].(map[string]interface{})["text"].(string)
			var ticketNumber, description string

			if text[1:6] == "large" {
				ticketNumber = text[31:38]
				description = string(text[40:])
			} else {
				ticketNumber = text[22:29]
				description = string(text[31:])
			}

			toRespTime, err := message.PostTicketMessage(respSeverity, description, userName, api, preCheckVal.toChannel, ticketNumber, "", assignedToName, StatusOpen)
			if err != nil {
				log.Error(err.Error())
				w.WriteHeader(http.StatusInternalServerError)
				return
			}

			//add `assignedTo` field as thread
			_, err = message.PostTicketMessage(respSeverity, description, userName, api, preCheckVal.fromChannel, ticketNumber, ts, assignedToName, StatusOpen)
			if err != nil {
				log.Error(err.Error())
				w.WriteHeader(http.StatusInternalServerError)
				return
			}

			// get details about `from` message
			var messageID int
			err = db.QueryRow(context.Background(), "SELECT id FROM analytics.askob_message where message_ts=$1", ts).Scan(&messageID)
			if err != nil {
				log.Error(os.Stderr, "QueryRow failed: %v", err)
				w.WriteHeader(http.StatusInternalServerError)
				return
			}
			var ticketID int
			err = db.QueryRow(context.Background(), "SELECT id FROM analytics.ticket where askob_message_id=$1", messageID).Scan(&ticketID)
			if err != nil {
				log.Error(os.Stderr, "QueryRow failed: %v", err)
				w.WriteHeader(http.StatusInternalServerError)
				return
			}
			// insert into message routing table twice(just change from_message_id and to_message_id) to establish comms between from and to channel messages

			//upserts through go routine
			go func() {
				messageID := routingUpserts(e, w, ts, toRespTime, jsonMsg["user"].(string), preCheckVal)

				data2 := make(map[string]interface{})
				data2["id"] = ticketID
				data2["title"] = "New Incident created"
				data2["description"] = description
				data2["canonical_id"] = ticketNumber
				data2["assigned_to"] = assignedTo
				data2["status"] = StatusOpen
				data2["created_by"] = userName
				data2["updated_by"] = userName
				data2["severity"] = respSeverity // typo to fix
				data2["askob_message_id"] = messageID
				message.TicketingInsert("PUT", w, ts, data2, e.obieContext.GetLog())
			}()
		}
	} else {
		for _, val := range msgs {
			marshalMsg, _ := json.Marshal(val)
			json.Unmarshal(marshalMsg, &jsonMsg)
			if jsonMsg["text"] != nil {
				msgSlice = append(msgSlice, jsonMsg["text"].(string))
			}

			if createdBy == "" {
				createdBy, err = getInfoHelper("User", jsonMsg["user"].(string), api)
				if err != nil {
					log.Error("%s\n", err)
					return
				}
			}
		}

		// it is the main message, so create ticket
		doParentMessageOperations(e, w, api, emoji, ts, curchannelID, preCheckVal, assignedTo, msgSlice, createdBy, SevMedium)
	}
}

// Helper method to handle emoji events
// 1. click  eyes to create ticket
// 2. click tick to resolve ticket
func doParentMessageOperations(e EventController, w http.ResponseWriter, api *slack.Client, emoji string, ts string, curchannelID string, preCheckVal *preCheckData, user string, input []string, createdBy string, severity string) {

	log := e.obieContext.GetLog()

	userName, err := getInfoHelper("User", user, api)
	if err != nil {
		log.Error("%s\n", err)
		return
	}
	if emoji == "eyes" {
		log.Info("creating ticket")
		createTicket(input, severity, createdBy, api, userName, ts, user, e, w, preCheckVal, curchannelID)
	} else if emoji == "white_check_mark" {
		log.Info("resolving ticket")
		resolveTicket(api, ts, userName, user, w, e, preCheckVal, curchannelID)
	}
}

// Helper method to resolve ticket
func resolveTicket(api *slack.Client, ts string, userName string, user string, w http.ResponseWriter, e EventController, preCheckVal *preCheckData, curchannelID string) {

	db := e.obieContext.GetDB()
	log := e.obieContext.GetLog()

	var messageID int
	err := db.QueryRow(context.Background(), "SELECT id FROM analytics.askob_message where message_ts=$1", ts).Scan(&messageID)
	if err != nil {
		log.Error(os.Stderr, "QueryRow failed: %v", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	
	createdBy := ""
	var ticketID int
	var ticketNumber, description, severity string
	err = db.QueryRow(context.Background(), "SELECT id, canonical_id, description, severity, ticket_created_by FROM analytics.ticket where askob_message_id=$1", messageID).Scan(&ticketID, &ticketNumber, &description, &severity, &createdBy)
	if err != nil {
		log.Error(os.Stderr, "QueryRow failed: %v", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// send update to `origin` channel
	myChannel := ""
	if preCheckVal != nil && preCheckVal.fromChannel != "" {
		myChannel = preCheckVal.fromChannel
	} else {
		myChannel = curchannelID
	}

	_, err = message.PostTicketMessage(severity, description, createdBy, api, myChannel, ticketNumber, ts, userName, StatusSolved)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// send update to `to` channel as well
	if preCheckVal != nil && preCheckVal.toChannel != "" {
		var toMessageID int
		var toChannel string
		err = db.QueryRow(context.Background(), "SELECT to_message_id, to_channel FROM analytics.message_routing where from_message_id=$1", messageID).Scan(&toMessageID, &toChannel)
		if err != nil {
			log.Error(os.Stderr, "QueryRow failed: %v", err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}

		var messageTS string
		err = db.QueryRow(context.Background(), "SELECT message_ts FROM analytics.askob_message where id=$1", toMessageID).Scan(&messageTS)
		if err != nil {
			log.Error(os.Stderr, "QueryRow failed: %v", err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}

		_, err = message.PostTicketMessage(severity, description, createdBy, api, toChannel, ticketNumber, messageTS, userName, StatusSolved)
		if err != nil {
			log.Error(err.Error())
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
	}

	go func() {
		data := make(map[string]interface{})
		data["id"] = ticketID
		data["status"] = StatusSolved
		data["title"] = "New Incident created"
		data["description"] = description
		data["canonical_id"] = ticketNumber
		data["assigned_to"] = user
		data["created_by"] = createdBy
		data["updated_by"] = createdBy
		data["severity"] = severity
		data["askob_message_id"] = messageID
		message.TicketingInsert("PUT", w, ts, data, e.obieContext.GetLog())
	}()
}

// Helper method to create ticket
func createTicket(input []string, severity string, createdBy string, api *slack.Client, userName string, ts string, user string, e EventController, w http.ResponseWriter, preCheckVal *preCheckData, curchannelID string) {

	log := e.obieContext.GetLog()
	var msg, respTimestamp string
	var err error

	//upserts through go routine
	go func() {
		msg = strings.Join(input, "\n")
		data2 := make(map[string]interface{})
		data2["title"] = "New Incident created"
		data2["description"] = msg
		// data2["canonical_id"] = encodeTicket
		data2["assigned_to"] = user
		data2["status"] = StatusOpen
		data2["created_by"] = createdBy
		data2["updated_by"] = createdBy
		data2["severity"] = severity // typo to fix

		// insert to ticket table to get uuid
		insertToTicketTableResult := message.TicketingInsert("POST", w, "", data2, e.obieContext.GetLog())
		if (insertToTicketTableResult["responseCode"] != float64(200)) && (insertToTicketTableResult["responseCode"] != float64(201)) {
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		ticketNumber := insertToTicketTableResult["entity"].(map[string]interface{})["canonical_id"].(string)

		// update slack to channel with status open
		if preCheckVal != nil && preCheckVal.toChannel != "" {
			respTimestamp, err = message.PostTicketMessage(severity, msg, createdBy, api, preCheckVal.toChannel, ticketNumber, "", userName, StatusOpen)
			if err != nil {
				log.Error(err.Error())
				w.WriteHeader(http.StatusInternalServerError)
				return
			}
		}

		myChannel := ""
		if preCheckVal != nil && preCheckVal.fromChannel != "" {
			myChannel = preCheckVal.fromChannel
		} else {
			myChannel = curchannelID
		}
		// update slack from channel with status open
		_, err = message.PostTicketMessage(severity, msg, createdBy, api, myChannel, ticketNumber, ts, userName, StatusOpen)
		if err != nil {
			log.Error(err.Error())
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		messageID := routingUpserts(e, w, ts, respTimestamp, user, preCheckVal)
		
		// insert to message table for the ticket created
		data2["askob_message_id"] = messageID
		data2["id"] = insertToTicketTableResult["entity"].(map[string]interface{})["id"].(float64)
		data2["canonical_id"] = ticketNumber
		// update ticket table with the message fk from previous step
		message.TicketingInsert("POST", w, ts, data2, e.obieContext.GetLog())
	}()
}

// Method to get real_name for given id
func getInfoHelper(inputType string, value string, api *slack.Client) (string, error) {
	var jsonMsg map[string]interface{}
	var outputInfo interface{}
	var err error
	if inputType == "User" {
		outputInfo, err = api.GetUserInfo(value)
	} else if inputType == "Channel" {
		outputInfo, err = api.GetConversationInfo(value, false)
	}
	if err != nil {
		return "", err
	}
	outputInfoTemp, _ := json.Marshal(outputInfo)
	json.Unmarshal(outputInfoTemp, &jsonMsg)
	if inputType == "User" {
		return jsonMsg["real_name"].(string), nil
	}
	return jsonMsg["name"].(string), nil
}

// Method to talk within threads on click of white_circle emoji
func talkWithinSlackThreads(e EventController, w http.ResponseWriter, jsonMsg map[string]interface{}, api *slack.Client) {
	db := e.obieContext.GetDB()
	log := e.obieContext.GetLog()
	var messageID, toMessageID int
	var messageTS, toChannel string
	var err error
	// 1. based on parent mssage ts get message_id (select message_id from message where ts = parent message ts)
	err = db.QueryRow(context.Background(), "SELECT id FROM analytics.askob_message where message_ts=$1", jsonMsg["thread_ts"]).Scan(&messageID)
	if err != nil {
		log.Error(os.Stderr, "askob_message 1 QueryRow failed: %v", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// 2. based on message_id get to_message_id (select to_message_id from message_routing where from_message_id = message_id)
	err = db.QueryRow(context.Background(), "SELECT to_message_id, to_channel FROM analytics.message_routing where from_message_id=$1", messageID).Scan(&toMessageID, &toChannel)
	if err != nil {
		log.Error(os.Stderr, "message_routing QueryRow failed: %v", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// 3. select ts form message where message_id = to_message_id
	err = db.QueryRow(context.Background(), "SELECT message_ts FROM analytics.askob_message where id=$1", toMessageID).Scan(&messageTS)
	if err != nil {
		log.Error(os.Stderr, "askob_message 2 QueryRow failed: %v", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	// 4. message.PostMessage(ts)
	msgThread := slack.MsgOption(slack.MsgOptionTS(messageTS))
	text := slack.MsgOptionText(jsonMsg["text"].(string), false)
	finalMsg := slack.MsgOptionCompose(msgThread, text)
	_, _, err = api.PostMessage(toChannel, finalMsg)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
}

// Helper method to upsert into Message and Message routing table
func routingUpserts(e EventController, w http.ResponseWriter, ts string, respTimestamp string, user string, preCheckVal *preCheckData) int {
	db := e.obieContext.GetDB()
	log := e.obieContext.GetLog()

	// inserting into message table twice to uniquely identify message in origin and to channels.
	messageID := -1
	var insertMessageResult map[string]interface{}
	err := db.QueryRow(context.Background(), "SELECT id FROM analytics.askob_message where message_ts=$1", ts).Scan(&messageID)
	
	if err != nil {
		log.Info(os.Stderr, "QueryRow failed: %v", err)
		if err == pgx.ErrNoRows {
			//insert into message only if not availble already. (will be inserted through slash command)
			insertMessageResult, err = message.InsertToMessageTable(log, user, ts)
			if err != nil {
				log.Error(err.Error())
				w.WriteHeader(http.StatusInternalServerError)
				return -1
			}
			if (insertMessageResult["responseCode"] != float64(200)) && (insertMessageResult["responseCode"] != float64(201)) {
				w.WriteHeader(http.StatusInternalServerError)
				return -1
			}
			messageID = int(insertMessageResult["entity"].(map[string]interface{})["id"].(float64))
		} else {
			w.WriteHeader(http.StatusInternalServerError)
			return -1
		}
	}

	if preCheckVal != nil {
		insertMessageResultDupe, err := message.InsertToMessageTable(log, user, respTimestamp)
		if err != nil {
			log.Error(err.Error())
			w.WriteHeader(http.StatusInternalServerError)
			return -1
		}

		if (insertMessageResultDupe["responseCode"] != float64(200)) && (insertMessageResultDupe["responseCode"] != float64(201)) {
			w.WriteHeader(http.StatusInternalServerError)
			return -1
		}

		// insert into message routing table twice(just change from_message_id and to_message_id) to establish comms between from and to channel messages
		updateMessageRouting(messageID, insertMessageResult, insertMessageResultDupe, preCheckVal, e, w)
	}

	return messageID
}

// Helper method to update message_routing table
func updateMessageRouting(messageID int, insertMessageResult map[string]interface{}, insertMessageResultDupe map[string]interface{}, preCheckVal *preCheckData, e EventController, w http.ResponseWriter) {

	log := e.obieContext.GetLog()

	data := make(map[string]interface{})
	if messageID != -1 {
		data["from_message_id"] = messageID
	} else {
		data["from_message_id"] = int(insertMessageResult["entity"].(map[string]interface{})["id"].(float64))
	}
	data["to_message_id"] = int(insertMessageResultDupe["entity"].(map[string]interface{})["id"].(float64))
	data["from_channel"] = preCheckVal.fromChannel
	data["to_channel"] = preCheckVal.toChannel
	data["from_workspace_id"] = preCheckVal.originWorkspaceID
	data["to_workspace_id"] = preCheckVal.toWorkspaceID
	insertMessageRoutingResult, err := message.InsertToMessageRoutingTable(log, data)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	if (insertMessageRoutingResult["responseCode"] != float64(200)) && (insertMessageRoutingResult["responseCode"] != float64(201)) {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	data1 := make(map[string]interface{})
	data1["from_message_id"] = int(insertMessageResultDupe["entity"].(map[string]interface{})["id"].(float64))
	if messageID != -1 {
		data1["to_message_id"] = messageID
	} else {
		data1["to_message_id"] = int(insertMessageResult["entity"].(map[string]interface{})["id"].(float64))
	}
	data1["from_channel"] = preCheckVal.toChannel
	data1["to_channel"] = preCheckVal.fromChannel
	data1["from_workspace_id"] = preCheckVal.toWorkspaceID
	data1["to_workspace_id"] = preCheckVal.originWorkspaceID
	insertMessageRoutingDupeResult, err := message.InsertToMessageRoutingTable(log, data1)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	if (insertMessageRoutingDupeResult["responseCode"] != float64(200)) && (insertMessageRoutingDupeResult["responseCode"] != float64(201)) {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
}

// Helper method to do prechecks before handling user events
func preChecks(e EventController, curchannelID string, w http.ResponseWriter) (*preCheckData, error) {

	log := e.obieContext.GetLog()
	db := e.obieContext.GetDB()

	var fromChannel, toChannel string
	var routingID, originWorkspaceID, toWorkspaceID int
	var err error
	toChannelCheck := false

	err = db.QueryRow(context.Background(), "SELECT channel_origin, channel_to, id, origin_workspace_id, to_workspace_id FROM analytics.askob_routing where channel_origin=$1", curchannelID).Scan(&fromChannel, &toChannel, &routingID, &originWorkspaceID, &toWorkspaceID)
	if err != nil {
		log.Info(os.Stderr, "Routing QueryRow failed: %v", err.Error())
		if err == pgx.ErrNoRows {
			err = db.QueryRow(context.Background(), "SELECT channel_origin, channel_to, id, origin_workspace_id, to_workspace_id FROM analytics.askob_routing where channel_to=$1", curchannelID).Scan(&fromChannel, &toChannel, &routingID, &originWorkspaceID, &toWorkspaceID)
			if err != nil && err == pgx.ErrNoRows {
				return nil, nil
			}
			toChannelCheck = true
		} else {
			return nil, err
		}
	}

	// pre-check logic: if current channel is not `from` or `to`, send error
	if curchannelID != fromChannel && curchannelID != toChannel {
		log.Info("you can only create ticket from `from` channel or send response to customer from `to` channel")
		return nil, nil
	}

	return &preCheckData{fromChannel: fromChannel, toChannel: toChannel, routingID: routingID, originWorkspaceID: originWorkspaceID, toWorkspaceID: toWorkspaceID, toChannelCheck: toChannelCheck}, nil
}
