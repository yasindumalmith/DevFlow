variable "namespace_name" {
  description = "Name of the Kubernetes namespace"
  type        = string
}

variable "owner_email" {
  description = "Owner of this environment"
  type        = string
}

variable "cpu_limit" {
  description = "CPU limit for the namespace"
  type        = string
  default     = "500m"
}

variable "memory_limit" {
  description = "Memory limit for the namespace"
  type        = string
  default     = "512Mi"
}