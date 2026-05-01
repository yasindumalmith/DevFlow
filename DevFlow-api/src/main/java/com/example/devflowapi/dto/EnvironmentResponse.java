package com.example.devflowapi.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnvironmentResponse {
    private String id;
    private String name;
    private String ownerEmail;
    private String status;
    private String namespace;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
}
