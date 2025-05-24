package modal

import "github.com/slack-go/slack"

func GenerateConfigure() slack.ModalViewRequest {
	titleText := slack.NewTextBlockObject("plain_text", "Configure", false, false)
	closeText := slack.NewTextBlockObject("plain_text", "Close", false, false)
	submitText := slack.NewTextBlockObject("plain_text", "Submit", false, false)

	headerText := slack.NewTextBlockObject("mrkdwn", "Admins can choose to redirect the problem statement from a user to a specific channel by selecting `to` channel. Admins can configure `to` channel at their wish from current channel.", false, false)
	headerSection := slack.NewSectionBlock(headerText, nil, nil)

	// fromChannelText := slack.NewTextBlockObject(slack.PlainTextType, "Select 'from' Channel", false, false)
	// fromChannelOption := slack.NewOptionsSelectBlockElement(slack.OptTypeConversations, fromChannelText, "select_from_channel")
	// fromChannelOption.DefaultToCurrentConversation = true
	// fromChannelOption.Filter = &slack.SelectBlockElementFilter{
	// 	Include:                       []string{"private", "public"},
	// 	ExcludeExternalSharedChannels: true,
	// 	ExcludeBotUsers:               false,
	// }
	// fromChannelSection := slack.NewSectionBlock(fromChannelText, nil, slack.NewAccessory(fromChannelOption))

	toChannelText := slack.NewTextBlockObject(slack.PlainTextType, "Select 'to' Channel", false, false)
	toChannelOption := slack.NewOptionsSelectBlockElement(slack.OptTypeConversations, toChannelText, "select_to_channel")
	toChannelOption.Filter = &slack.SelectBlockElementFilter{
		Include:                       []string{"private", "public"},
		ExcludeExternalSharedChannels: true,
		ExcludeBotUsers:               false,
	}
	toChannelSection := slack.NewSectionBlock(toChannelText, nil, slack.NewAccessory(toChannelOption))

	blocks := slack.Blocks{
		BlockSet: []slack.Block{
			headerSection,
			//fromChannelSection,
			toChannelSection,
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
