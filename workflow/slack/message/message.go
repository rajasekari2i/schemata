package message

import (
	"fmt"
	"os"
	"net/http"
	obhttp "workflow/slack-app/http"	

	"github.com/sirupsen/logrus"
	"github.com/slack-go/slack"
)

const (
	SevLow = "LOW"
	SevMedium = "MEDIUM"
	SevHigh = "HIGH"
)

func PostTicketMessage(respSeverity string, respSummary string, userName string, api *slack.Client, channelName string, ticketNumber string, ts string, assignedTo string, status string) (string, error) {

	var respTimestamp string
	var err error

	var attachmentField []slack.AttachmentField
	field1 := slack.AttachmentField{
		Title: "Priority:",
		Value: respSeverity,
	}

	field2 := slack.AttachmentField{
		Title: "Created By:",
		Value: userName,
	}

	field3 := slack.AttachmentField{
		Title: "Status:",
		Value: status,
	}

	attachmentField = append(attachmentField, field1, field2, field3)

	var field4 slack.AttachmentField
	if assignedTo != "" {
		field4 = slack.AttachmentField{
			Title: "Assigend To:",
			Value: assignedTo,
		}
		attachmentField = append(attachmentField, field4)
	}

	attachment := slack.Attachment{
		Title:  "Details:",
		Fields: attachmentField,
	}

	switch respSeverity {
	case SevHigh:
		{
			attachment.Color = "#FF0000"
			attachment.Text = fmt.Sprintf(":red_circle: [Ticket: %s] %s ", ticketNumber, respSummary)
		}
	case SevMedium:
		{
			attachment.Color = "#FFFF00"
			attachment.Text = fmt.Sprintf(":large_yellow_circle: [Ticket: %s] %s ", ticketNumber, respSummary)
		}
	case SevLow:
		{
			attachment.Color = "#00FF00"
			attachment.Text = fmt.Sprintf(":large_green_circle: [Ticket: %s] %s ", ticketNumber, respSummary)
		}
	}

	msgAttachment := slack.MsgOption(slack.MsgOptionAttachments(attachment))

	var msgThread, finalMsg slack.MsgOption

	if ts != "" {
		//post message in a thread
		msgThread = slack.MsgOption(slack.MsgOptionTS(ts))
		finalMsg = slack.MsgOptionCompose(msgAttachment, msgThread)
	} else {
		finalMsg = slack.MsgOptionCompose(msgAttachment)
	}

	_, respTimestamp, err = api.PostMessage(
		channelName,
		finalMsg)
	return respTimestamp, err
}

// insert into message table
func InsertToMessageTable(log *logrus.Logger, user string, respTimestamp string) (map[string]interface{}, error) {
	insertMessageUrl := os.Getenv("CONNECT_BASE_URL") + "message"
	// store data in askob_routing table
	data := make(map[string]interface{})
	data["message_ts"] = respTimestamp
	data["type"] = "SLACK"
	data["message_user_id"] = user
	return obhttp.MakeHttpCall(log, "POST", insertMessageUrl, data)
}

// insert into message routing table
func InsertToMessageRoutingTable(log *logrus.Logger, data map[string]interface{}) (map[string]interface{}, error) {
	insertMessageRoutingUrl := os.Getenv("CONNECT_BASE_URL") + "message-routing"
	// store data in askob_routing table
	return obhttp.MakeHttpCall(log, "POST", insertMessageRoutingUrl, data)
}

//insert to ticket table
func InsertToTicketTable(log *logrus.Logger, method string, data map[string]interface{}) (map[string]interface{}, error) {
	insertTicketUrl := os.Getenv("CONNECT_BASE_URL") + "ticket"
	// store data in askob_routing table
	return obhttp.MakeHttpCall(log, method, insertTicketUrl, data)
}

// Helper method to insert into ticket table
func TicketingInsert(method string, w http.ResponseWriter, ts string, inputdata map[string]interface{}, log *logrus.Logger) map[string]interface{} {
	
	insertToTicketTableResult, err := InsertToTicketTable(log, method, inputdata)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return nil
	}
	if (insertToTicketTableResult["responseCode"] != float64(200)) && (insertToTicketTableResult["responseCode"] != float64(201)) {
		w.WriteHeader(http.StatusInternalServerError)
		return nil
	}
	return insertToTicketTableResult
}