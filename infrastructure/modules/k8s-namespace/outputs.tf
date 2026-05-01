output "namespace_name" {
  value = kubernetes_namespace.env_namespace.metadata[0].name
}

output "service_account" {
  value = kubernetes_service_account.env_service_account.metadata[0].name
}