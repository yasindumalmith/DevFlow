package com.example.devflowapi.mapper;

import com.example.devflowapi.dto.EnvironmentResponse;
import com.example.devflowapi.model.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentMapper {

    public EnvironmentResponse toResponse(Environment environment) {
        return EnvironmentResponse.builder()
                .id(environment.getId())
                .name(environment.getName())
                .ownerEmail(environment.getOwnerEmail())
                .status(environment.getStatus() != null ? environment.getStatus().name() : null)
                .namespace(environment.getNamespace())
                .createdAt(environment.getCreatedAt())
                .lastActiveAt(environment.getLastActiveAt())
                .build();
    }
}