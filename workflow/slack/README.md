Halo App

How to respond to a slash command with an interactive modal and parse the response

The flow of this example:
 1. User trigers your app with a slash command (e.g. /helpme) that will send a request to http://URL/slash and respond with a request to open a modal
 2. User fills out fields and hits submit
 3. This will send a request to http://URL/modal


Note: Be sure to update SLACK_BOT_TOKEN and SLACK_VERIFICATION_TOKEN
You can use ngrok to test this example: https://api.slack.com/tutorials/tunneling-with-ngrok

#To Run:
go mod tidy
go run main.go

#Useful docker commands:
docker build . --tag slack-app
docker exec -it 9b9f82b0692d  bash

docker run -p 8000:8000 --env SIGN_IN_SECRET=$SIGN_IN_SECRET --env SLACK_VERIFICATION_TOKEN=$SLACK_VERIFICATION_TOKEN --env SLACK_BOT_TOKEN=$SLACK_BOT_TOKEN slack-app

docker rm $(docker ps -aq)
docker rmi $(docker images -q)

kubectl exec --stdin --tty slack-app-deployment-5494cf868c-bdwp4 -- /bin/bash
kubectl exec -it slack-app-deployment-5494cf868c-bdwp4 -- /bin/bash

#TODO:
1. docker secrets to store api tokens
2. configuration file

#Create artifact registry
gcloud auth login
gcloud config set project just-site-344717

gcloud artifacts repositories create opsconnect-repo \
--project=just-site-344717 \
--repository-format=docker \
--location=us-east1 \
--description="Docker repository"

gcloud builds submit \
--tag us-east1-docker.pkg.dev/just-site-344717/opsconnect-repo/workflow/slack-app .

#Useful terraform commands
terraform init
terraform plan -out tf.plan
terraform apply tf.plan


gcloud container clusters create opsconnect-cluster --num-nodes 1 --zone us-east1

gcloud container clusters get-credentials opsconnect-cluster --region us-east1 --project just-site-344717
gcloud container clusters delete opsconnect-cluster --zone us-east1

gcloud beta compute ssl-certificates create ops-beach-cert --project=just-site-344717 --global --domains=hola.opebeach.com
