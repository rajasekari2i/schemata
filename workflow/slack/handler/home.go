package handler

import (
	"encoding/json"
	"net/http"
	"os"
	"workflow/slack-app/base"

	modal "workflow/slack-app/modal"

	"github.com/slack-go/slack"
)

type HomeController struct {
	obieContext *base.ObieContext
}

func NewHomeController(context *base.ObieContext) *HomeController {
	return &HomeController{
		obieContext: context,
	}
}

// Method to handle slash commands from app
func (h HomeController) Home(w http.ResponseWriter, r *http.Request) {

	log := h.obieContext.GetLog()
	s, err := slack.SlashCommandParse(r)

	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	if !s.ValidateToken(os.Getenv("SLACK_VERIFICATION_TOKEN")) {
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	api := slack.New(os.Getenv("SLACK_BOT_TOKEN"))
	switch s.Command {

	// open UI based on input text from slack app

	//case "/testme":
	case "/obie":
		switch s.Text {
		case "configure":
			{
				// open configure screen
				modalRequest := modal.GenerateConfigure()
				modalRequest.PrivateMetadata = s.ChannelID
				_, err = api.OpenView(s.TriggerID, modalRequest)
				if err != nil {
					log.Error("Error opening view: %s", err)
					w.WriteHeader(http.StatusInternalServerError)
					return
				}
			}
		case "help":
			{
				// open help screen
				params := &slack.Msg{Blocks: modal.GenerateHome()}
				b, err := json.Marshal(params)
				if err != nil {
					w.WriteHeader(http.StatusInternalServerError)
					return
				}
				w.Header().Set("Content-Type", "application/json")
				w.Write(b)
			}
		default:
			{
				// open incident screen and set current channel in private metadata
				modalRequest := modal.GenerateIncident(s.Text)
				modalRequest.PrivateMetadata = s.ChannelID
				_, err = api.OpenView(s.TriggerID, modalRequest)
				if err != nil {
					log.Error("Error opening view: %s", err)
					w.WriteHeader(http.StatusInternalServerError)
					return
				}
			}
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
