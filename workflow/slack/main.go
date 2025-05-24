package main

import (
	"fmt"
	"net/http"
	"workflow/slack-app/base"
	handler "workflow/slack-app/handler"

	log "github.com/sirupsen/logrus"
)

func healthCheck(w http.ResponseWriter, r *http.Request) {
	fmt.Println("Hello from k8s cluster!!!")
}

func main() {
	ctx := base.NewObieContext()
	log.Info("DB Connection & Context initialized")
	homeHandler := handler.NewHomeController(ctx)
	oauthHandler := handler.NewOauthController(ctx)
	modalHandler := handler.NewModalController(ctx)
	eventHandler := handler.NewEventController(ctx)

	http.HandleFunc("/", healthCheck)
	http.HandleFunc("/home", homeHandler.Home)
	http.HandleFunc("/oauth", oauthHandler.Oauth)
	http.HandleFunc("/modal", modalHandler.Modal)
	http.HandleFunc("/event", eventHandler.Event)

	log.Info("Server listening")
	http.ListenAndServe(":8000", nil)

}
