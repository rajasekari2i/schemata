# opsconnect
Repo for OpsBeach

# compaile
maven clean install

# run
Run as spring boot application

# create secrets for service accounts

https://cloud.google.com/kubernetes-engine/docs/tutorials/authenticating-to-cloud-platform#importing_credentials_as_a_secret
`
kubectl -n schematalabs create secret generic cloud-task-sa-key --from-file=cloud-task-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/cloud_tasks_sa_cred.json
kubectl -n schematalabs create secret generic cluster-sa-key --from-file=cluster-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/cluster_sa_cred.json
kubectl -n schematalabs create secret generic compute-sa-key --from-file=compute-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/compute_sa_cred.json
kubectl -n schematalabs create secret generic bucket-sa-key --from-file=bucket-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/buckets_sa_cred.json
`
# infra setup

gcloud config set project prodenv1
create vpc/subnet
create cluster
install gcloud/gcloud components update
gcloud init
gcloud auth login
gcloud config set project just-site-344717
gcloud container clusters get-credentials opsconnect --zone us-central1-c --project just-site-344717

gcloud set project prodenv1
gcloud container clusters get-credentials schematalabs --region us-central1 --project prodenv1
kubectl get nodes

create artifact registry
create cloud task queue
create sql postgres, create db opsbeach
create neo4j instance

gcloud compute firewall-rules create schematalabs-neo4j-https    --allow tcp:7473,tcp:7687    --source-ranges 0.0.0.0/0    --target-tags neo4j --network schematalabs-vpc

gcloud compute images list

gcloud compute instances create schematalabs-neo4j  --network-interface=network=schematalabs-vpc,subnet=schematalabs-subnet  --scopes https://www.googleapis.com/auth/cloud-platform    --image-project launcher-public --tags neo4j    --image=neo4j-community-1-4-3-6-apoc


ssh into vm, sudo vi /etc/neo4j/neo4j.conf,
dbms.default_advertised_address=35.238.222.192

# Bolt connector
dbms.connector.bolt.enabled=true
#dbms.connector.bolt.tls_level=DISABLED
dbms.connector.bolt.listen_address=0.0.0.0:7687
dbms.connector.bolt.advertised_address=35.238.222.192:7687

# HTTP Connector. There can be zero or one HTTP connectors.
dbms.connector.http.enabled=true
dbms.connector.http.listen_address=0.0.0.0:7474
dbms.connector.http.advertised_address=35.238.222.192:7474

# HTTPS Connector. There can be zero or one HTTPS connectors.
dbms.connector.https.enabled=false
#dbms.connector.https.listen_address=0.0.0.0:7473
#dbms.connector.https.advertised_address=35.238.222.192:7473
setup ssl policy for https

neo4j://35.238.222.192:7687


---

change cloudbuild, manifest files to point to correct Artifact repository,cluster(name, zone, region)

create bucket, compute, cluster, cloud tasks sa
create actual bukcts to store bootstrap data
update path in config files

create secrets...
kubectl -n schematalabs create secret generic cloud-task-sa-key --from-file=cloud-task-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/cloud_tasks_sa_cred.json
kubectl -n schematalabs create secret generic cluster-sa-key --from-file=cluster-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/cluster_sa_cred.json
kubectl -n schematalabs create secret generic compute-sa-key --from-file=compute-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/compute_sa_cred.json
kubectl -n schematalabs create secret generic bucket-sa-key --from-file=bucket-sa-key.json=/Users/sathish/workspace/opsconnect/aldefi-sa/buckets_sa_cred.json

kubectl apply frontendconfig, ingress



docker run -p7474:7474 -p7687:7687 -v $HOME/neo4j/data:/data -v $HOME/neo4j/logs:/logs -v $HOME/neo4j/import:/var/lib/neo4j/import -v $HOME/neo4j/plugins:/plugins --env NEO4J_AUTH=neo4j/password --env NEO4J_PLUGINS='["apoc"]' --env NEO4J_apoc_export_file_enabled=true --env NEO4J_apoc_import_file_enabled=true --env NEO4J_dbms_security_procedures_unrestricted='*' us-docker.pkg.dev/prodenv1/dozer-nio4j/graphstack/dozerdb


gcloud beta container --project "prodenv1" clusters create-auto "autopilot-cluster-1" --region "us-central1" --release-channel "regular" --network "projects/prodenv1/global/networks/schematalabs-vpc" --subnetwork "projects/prodenv1/regions/us-central1/subnetworks/schematalabs-subnet" --cluster-ipv4-cidr "/17" --binauthz-evaluation-mode=DISABLED
