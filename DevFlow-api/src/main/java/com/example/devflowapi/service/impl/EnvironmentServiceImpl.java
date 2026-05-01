package com.example.devflowapi.service.impl;

import com.example.devflowapi.dto.CreateEnvironmentRequest;
import com.example.devflowapi.dto.EnvironmentResponse;
import com.example.devflowapi.mapper.EnvironmentMapper;
import com.example.devflowapi.model.AuditLog;
import com.example.devflowapi.model.Environment;
import com.example.devflowapi.model.EnvironmentStatus;
import com.example.devflowapi.repository.AuditLogRepository;
import com.example.devflowapi.repository.EnvironmentRepository;
import com.example.devflowapi.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentServiceImpl implements EnvironmentService {
    private final EnvironmentRepository environmentRepository;
    private final AuditLogRepository auditLogRepository;

    public EnvironmentResponse createEnvironment(CreateEnvironmentRequest request) {
        log.info("Creating environment: {}", request.getName());

        Environment env = Environment.builder()
                .name(request.getName())
                .ownerEmail(request.getOwnerEmail())
                .namespace("devflow-" + request.getName().toLowerCase())
                .build();

        env = environmentRepository.save(env);

        auditLog(env.getId(), "CREATE", request.getOwnerEmail(),
                "Environment creation initiated");

        // Terraform call comes in Phase 2 — for now just save
        env.setStatus(EnvironmentStatus.RUNNING);
        environmentRepository.save(env);

        return toResponse(env);
    }
    public List<EnvironmentResponse> getAllEnvironments() {
        return environmentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EnvironmentResponse getEnvironment(String id) {
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found: " + id));
        return toResponse(env);
    }

    public void deleteEnvironment(String id, String deletedBy) {
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Environment not found: " + id));

        env.setStatus(EnvironmentStatus.DESTROYING);
        environmentRepository.save(env);

        auditLog(id, "DELETE", deletedBy, "Environment destruction initiated");

        // Terraform destroy comes in Phase 2
        env.setStatus(EnvironmentStatus.DESTROYED);
        env.setDestroyedAt(LocalDateTime.now());
        environmentRepository.save(env);
    }

    private void auditLog(String envId, String action,
                          String by, String details) {
        auditLogRepository.save(AuditLog.builder()
                .environmentId(envId)
                .action(action)
                .performedBy(by)
                .details(details)
                .build());
    }
    private EnvironmentResponse toResponse(Environment env) {
        return EnvironmentResponse.builder()
                .id(env.getId())
                .name(env.getName())
                .ownerEmail(env.getOwnerEmail())
                .status(env.getStatus().name())
                .namespace(env.getNamespace())
                .createdAt(env.getCreatedAt())
                .lastActiveAt(env.getLastActiveAt())
                .build();
    }
}
