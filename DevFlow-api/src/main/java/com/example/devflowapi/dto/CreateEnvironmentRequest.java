package com.example.devflowapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEnvironmentRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Owner email is required")
    private String ownerEmail;
}
