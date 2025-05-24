package http

import (
	"bytes"
	"encoding/json"	
	"io/ioutil"
	"net/http"
	"os"

	"github.com/sirupsen/logrus"
)

func MakeHttpCall(log *logrus.Logger, method string, url string, data map[string]interface{}) (map[string]interface{}, error) {

	input, err := json.Marshal(data)
	if err != nil {
		log.Error(err.Error())
	}

	var req *http.Request
	if method == "GET" {
		req, err = http.NewRequest(
			method,
			url,
			nil,
		)
	} else {
		req, err = http.NewRequest(
			method,
			url,
			bytes.NewBuffer(input),
		)
	}

	if err != nil {
		log.Error(err.Error())
		return nil, err
	}

	auth := "Bearer " + os.Getenv("CONNECT_ACCESS_TOKEN")
	req.Header.Set("Content-Type", "application/json")
	req.Header.Add("Authorization", auth)	
	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Error(err.Error())
		return nil, err
	}

	defer resp.Body.Close()
	
	body, err := ioutil.ReadAll(resp.Body)	
	if err != nil {
		log.Error(err.Error())
		return nil, err
	}
	var result map[string]interface{}
	json.Unmarshal(body, &result)	
	return result, nil
}
