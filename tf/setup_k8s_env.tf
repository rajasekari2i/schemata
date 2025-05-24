# Needs to be checked for env (dev/prod) and variables.
# probably a config map?

# Creates GKE
provider "google" {
  project     = "just-site-344717"
  region      = "us-central1-c"
  credentials = file("../sa_credentials.json")
}

resource "google_container_node_pool" "primary_preemptible_nodes" {
  name       = "my-node-pool"
  location   = "us-central1-c"
  cluster    = google_container_cluster.primary.name
  node_count = 1

  node_config {
    preemptible  = true
    machine_type = "e2-medium"    
  }
}

resource "google_container_cluster" "primary" {
  name     = "opsconnect-cluster"
  location = "us-central1-c"

  # We can't create a cluster with no node pool defined, but we want to only use
  # separately managed node pools. So we create the smallest possible default
  # node pool and immediately delete it.
  remove_default_node_pool = true
  initial_node_count       = 1
}

# creates static IP to attach to ingress and DNS A record
resource "google_compute_global_address" "default" {
  name = "global-appserver-ip"
  ip_version = "IPV4"
}

# creates google managed ssl certificate
resource "google_compute_managed_ssl_certificate" "default" {
  name = "google-managed-cert"

  managed {
    domains = ["hola.opsbeach.com"]
  }
}

#setup ingress to ensure we access through https
resource "kubernetes_ingress" "slack-app-ingress" {
  metadata {
    name = "slack-app-ingress"
  }

  annotations = {
    "kubernetes.io/ingress.global-static-ip-name" = "test-address"
    "networking.gke.io/managed-certificates"      = "google-managed-cert"
    "kubernetes.io/ingress.class"                 = "gce"
  }

  spec {
    backend {
      service_name = "slack-app-service"
      service_port = 80
    }
  }
}