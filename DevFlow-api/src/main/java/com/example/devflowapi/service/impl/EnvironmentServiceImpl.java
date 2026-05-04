package com.example.devflowapi.service.impl;

import com.example.devflowapi.dto.CreateEnvironmentRequest;
import com.example.devflowapi.dto.EnvironmentResponse;
import com.example.devflowapi.kubernetes.KubernetesService;
import com.example.devflowapi.mapper.EnvironmentMapper;
import com.example.devflowapi.model.AuditLog;
import com.example.devflowapi.model.Environment;
import com.example.devflowapi.model.EnvironmentStatus;
import com.example.devflowapi.repository.AuditLogRepository;
import com.example.devflowapi.repository.EnvironmentRepository;
import com.example.devflowapi.service.EnvironmentService;
import com.example.devflowapi.terraform.TerraformRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentServiceImpl implements EnvironmentService {
    private final EnvironmentRepository environmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final TerraformRunner terraformRunner;
    private final KubernetesService kubernetesService;

    public EnvironmentResponse createEnvironment(CreateEnvironmentRequest request) {
        log.info("Creating environment: {}", request.getName());

        String namespaceName = "devflow-" +
                request.getName().toLowerCase().replaceAll("[^a-z0-9-]", "-");

        Environment env = Environment.builder()
                .name(request.getName())
                .ownerEmail(request.getOwnerEmail())
                .namespace("devflow-" + request.getName().toLowerCase())
                .build();

        env = environmentRepository.save(env);
        final String envId = env.getId();

        auditLog(env.getId(), "CREATE", request.getOwnerEmail(),
                "Environment creation initiated");

        // Terraform call comes in Phase 2 — for now just save
        final Environment savedEnv = env;
        CompletableFuture.runAsync(() -> {
            try {
                terraformRunner.provision(namespaceName,
                        request.getOwnerEmail());

                savedEnv.setStatus(EnvironmentStatus.RUNNING);
                environmentRepository.save(savedEnv);

                auditLog(envId, "PROVISIONED",
                        request.getOwnerEmail(),
                        "Namespace created in Kubernetes");

                log.info("Environment provisioned: {}", namespaceName);

            } catch (Exception e) {
                log.error("Provisioning failed for {}", namespaceName, e);
                savedEnv.setStatus(EnvironmentStatus.FAILED);
                environmentRepository.save(savedEnv);
                auditLog(envId, "FAILED", request.getOwnerEmail(),
                        "Error: " + e.getMessage());
            }
        });

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
        CompletableFuture.runAsync(() -> {
            try {
                terraformRunner.destroy(env.getNamespace(),
                        env.getOwnerEmail());

                env.setStatus(EnvironmentStatus.DESTROYED);
                env.setDestroyedAt(LocalDateTime.now());
                environmentRepository.save(env);

                auditLog(id, "DESTROYED", deletedBy,
                        "Namespace removed from Kubernetes");

            } catch (Exception e) {
                log.error("Destroy failed for {}", env.getNamespace(), e);
                env.setStatus(EnvironmentStatus.FAILED);
                environmentRepository.save(env);
            }
        });
    }
    public Map<String, Object> getEnvironmentHealth(String id) {
        Environment env = environmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Environment not found: " + id));

        // Update last active timestamp
        env.setLastActiveAt(LocalDateTime.now());
        environmentRepository.save(env);

        return kubernetesService.getNamespaceStatus(env.getNamespace());
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
