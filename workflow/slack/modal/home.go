package modal

import "github.com/slack-go/slack"

func GenerateHome() slack.Blocks {
	headerText := slack.NewTextBlockObject("mrkdwn", "Hi there, I'm OpsBeach :wave: I am here to help you with your problem. Here are some quick tips to get you started.!\n\n", false, false)
	headerSection := slack.NewSectionBlock(headerText, nil, nil)

	dividerSection := slack.NewDividerBlock()

	bodyText := slack.NewTextBlockObject("mrkdwn", "*Basics*\n You can describe your problem in Slack the following ways :\n • `/askob can someone please help me with my keyboard?` that opens a form to create a ticket \n • Admins have to configure `/askob configure` a channel to which they want to send the customer problem to resolve internally  \n • You can just post your problem in the channel and agent creates a ticket for you", false, false)
	bodySection := slack.NewSectionBlock(bodyText, nil, nil)

	// configureBtnTxt := slack.NewTextBlockObject("plain_text", "Configure", false, false)
	// configureBtn := slack.NewButtonBlockElement("", "configure", configureBtnTxt)

	// createTicketBtnTxt := slack.NewTextBlockObject("plain_text", "Create Ticket", false, false)
	// createTicketBtn := slack.NewButtonBlockElement("", "createTicket", createTicketBtnTxt)

	// actionSection := slack.NewActionBlock("", configureBtn, createTicketBtn)

	blocks := slack.Blocks{
		BlockSet: []slack.Block{
			headerSection,
			dividerSection,
			bodySection,
			dividerSection,
			//actionSection,
			//dividerSection,
		},
	}

	return blocks
}
