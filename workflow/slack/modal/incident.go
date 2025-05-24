package modal

import "github.com/slack-go/slack"

const (
	SevLow    = "LOW"
	SevMedium = "MEDIUM"
	SevHigh   = "HIGH"
)

func GenerateIncident(input string) slack.ModalViewRequest {
	//radio button
	radioButtonsOptionTextOne := slack.NewTextBlockObject("plain_text", SevLow+" :large_green_circle:", false, false)
	radioButtonsOptionTextTwo := slack.NewTextBlockObject("plain_text", SevMedium+" :large_yellow_circle:", false, false)
	radioButtonsOptionTextThree := slack.NewTextBlockObject("plain_text", SevHigh+" :red_circle:", false, false)

	// Build each option, providing a value for the option
	radioButtonsOptionOne := slack.NewOptionBlockObject(SevLow, radioButtonsOptionTextOne, nil)
	radioButtonsOptionTwo := slack.NewOptionBlockObject(SevMedium, radioButtonsOptionTextTwo, nil)
	radioButtonsOptionThree := slack.NewOptionBlockObject(SevHigh, radioButtonsOptionTextThree, nil)

	// Build radio button element
	radioButtonsElement := slack.NewRadioButtonsBlockElement("severity", radioButtonsOptionOne, radioButtonsOptionTwo, radioButtonsOptionThree)
	radioText := slack.NewTextBlockObject("mrkdwn", "Severity", false, false)
	radioButtons := slack.NewSectionBlock(radioText, nil, slack.NewAccessory(radioButtonsElement))

	titleText := slack.NewTextBlockObject("plain_text", "AskOB", false, false)
	closeText := slack.NewTextBlockObject("plain_text", "Close", false, false)
	submitText := slack.NewTextBlockObject("plain_text", "Submit", false, false)

	headerText := slack.NewTextBlockObject("mrkdwn", "Hello, Assistant to the Regional Manager Dwight! *Michael Scott* wants to know about your problem\n\n", false, false)
	headerSection := slack.NewSectionBlock(headerText, nil, nil)

	dividerSection := slack.NewDividerBlock()

	// Incident summary
	summaryText := slack.NewTextBlockObject("plain_text", "Summary", false, false)
	summaryPlaceholder := slack.NewTextBlockObject("plain_text", "Tell us about your problem...", false, false)
	summaryElement := slack.NewPlainTextInputBlockElement(summaryPlaceholder, "summary")
	summaryElement.MaxLength = 200
	summaryElement.Multiline = true
	summaryElement.InitialValue = input
	summary := slack.NewInputBlock("Summary", summaryText, summaryElement)

	blocks := slack.Blocks{
		BlockSet: []slack.Block{
			headerSection,
			dividerSection,
			summary,
			radioButtons,
		},
	}

	var modalRequest slack.ModalViewRequest
	modalRequest.Type = slack.ViewType("modal")
	modalRequest.Title = titleText
	modalRequest.Close = closeText
	modalRequest.Submit = submitText
	modalRequest.Blocks = blocks
	return modalRequest
}
