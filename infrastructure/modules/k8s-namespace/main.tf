terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
  }
}

provider "kubernetes" {
  config_path    = "~/.kube/config"
  config_context = "minikube"
}

# Create the namespace
resource "kubernetes_namespace" "env_namespace" {
  metadata {
    name = var.namespace_name
    labels = {
      managed-by  = "devflow"
      owner       = var.owner_email
      environment = var.namespace_name
    }
  }
}

# Resource quota — limits total CPU and memory in this namespace
resource "kubernetes_resource_quota" "env_quota" {
  metadata {
    name      = "${var.namespace_name}-quota"
    namespace = kubernetes_namespace.env_namespace.metadata[0].name
  }

  spec {
    hard = {
      "requests.cpu"    = var.cpu_limit
      "requests.memory" = var.memory_limit
      "limits.cpu"      = var.cpu_limit
      "limits.memory"   = var.memory_limit
      "pods"            = "10"
    }
  }
}

# Network policy — only allow traffic within the namespace
resource "kubernetes_network_policy" "env_network_policy" {
  metadata {
    name      = "${var.namespace_name}-network-policy"
    namespace = kubernetes_namespace.env_namespace.metadata[0].name
  }

  spec {
    pod_selector {}

    ingress {
      from {
        namespace_selector {
          match_labels = {
            "kubernetes.io/metadata.name" = var.namespace_name
          }
        }
      }
    }

    policy_types = ["Ingress"]
  }
}

# Service account for this environment
resource "kubernetes_service_account" "env_service_account" {
  metadata {
    name      = "${var.namespace_name}-sa"
    namespace = kubernetes_namespace.env_namespace.metadata[0].name
  }
}