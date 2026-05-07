package com.example.devflowapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TriggerDeploymentRequest {
    @NotBlank
    private String imageTag;

    @NotBlank
    private String triggeredBy;
}
