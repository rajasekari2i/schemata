package handler

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"strings"
	"workflow/slack-app/base"
	obhttp "workflow/slack-app/http"
)

type OauthController struct {
	obieContext *base.ObieContext
}

func NewOauthController(context *base.ObieContext) *OauthController {
	return &OauthController{
		obieContext: context,
	}
}

func (o OauthController) Oauth(w http.ResponseWriter, r *http.Request) {
	log := o.obieContext.GetLog()

	log.Info("authenticating user...")
	// get slack access token
	resp := getSlackAccessToken(w, r, o)
	defer resp.Body.Close()
	if resp == nil {
		log.Error("Nil response from slack")
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	//unstructured json includes both oauth pass and fail case
	var result map[string]interface{}
	json.Unmarshal(body, &result)

	//send 401 on oauth fail else store authToken
	if result["ok"] == false {
		log.Error(result["error"])
		w.WriteHeader(http.StatusUnauthorized)
		w.Header().Set("Content-Type", "application/json")
		resp := make(map[string]string)
		resp["message"] = "Unauthorized"
		jsonResp, err := json.Marshal(resp)
		if err != nil {
			log.Error("Error happened in JSON marshal. Err: %s", err)
		}
		w.Write(jsonResp)
		return
	}

	// store result in askob_workspace table
	upsertNewUser(result, o, w, r)
}

func upsertNewUser(result map[string]interface{}, o OauthController, w http.ResponseWriter, r *http.Request) {
	log := o.obieContext.GetLog()
	data := make(map[string]interface{})
	data["token"] = result["access_token"]
	data["key"] = result["team"].(map[string]interface{})["id"].(string)
	data["name"] = result["team"].(map[string]interface{})["name"].(string)
	data["user_source_id"] = result["authed_user"].(map[string]interface{})["id"].(string)
	data["type"] = "SLACK"

	connectWorkspaceUrl := os.Getenv("CONNECT_BASE_URL") + "workspace"
	result, err := obhttp.MakeHttpCall(log, "POST", connectWorkspaceUrl, data)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return
	}

	if result["responseCode"] == float64(500) {
		if strings.Contains(result["message"].(string), "constraint [askob_workspace_user_source_id_key]") {
			log.Info("User already added.")
		} else {
			log.Error(result["message"].(string))
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
	}

	//ideally should transfer to opsbeach homepage or a thank you screen?
	log.Info("App installation success")
	http.Redirect(w, r, "https://www.opsbeach.com", http.StatusSeeOther)

}

func getSlackAccessToken(w http.ResponseWriter, r *http.Request, o OauthController) *http.Response {

	log := o.obieContext.GetLog()
	clientID := os.Getenv("CLIENT_ID")
	clientSecret := os.Getenv("CLIENT_SECRET")

	code := r.URL.Query().Get("code")
	values := url.Values{}
	values.Add("code", code)
	values.Add("client_id", clientID)
	values.Add("client_secret", clientSecret)

	req, err := http.NewRequest(
		"POST",
		"https://slack.com/api/oauth.v2.access",
		strings.NewReader(values.Encode()),
	)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return nil
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Error(err.Error())
		w.WriteHeader(http.StatusInternalServerError)
		return nil
	}

	return resp
}
