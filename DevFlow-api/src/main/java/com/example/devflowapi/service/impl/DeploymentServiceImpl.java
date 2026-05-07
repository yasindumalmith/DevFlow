package com.example.devflowapi.service.impl;

import com.example.devflowapi.dto.TriggerDeploymentRequest;
import com.example.devflowapi.github.GithubActionsClient;
import com.example.devflowapi.model.*;
import com.example.devflowapi.repository.AuditLogRepository;
import com.example.devflowapi.repository.DeploymentRepository;
import com.example.devflowapi.repository.EnvironmentRepository;
import com.example.devflowapi.service.DeploymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeploymentServiceImpl implements DeploymentService {
    private final DeploymentRepository deploymentRepository;
    private final EnvironmentRepository environmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final GithubActionsClient gitHubActionsClient;

    @Override
    public Deployment triggerDeployment(String environmentId, TriggerDeploymentRequest request) {
        Environment env = environmentRepository.findById(environmentId)
                .orElseThrow(() ->
                        new RuntimeException("Environment not found"));

        if (env.getStatus() != EnvironmentStatus.RUNNING) {
            throw new RuntimeException(
                    "Environment is not in RUNNING state. Current: "
                            + env.getStatus());
        }

        // Save deployment record
        Deployment deployment = Deployment.builder()
                .environmentId(environmentId)
                .imageTag(request.getImageTag())
                .triggeredBy(request.getTriggeredBy())
                .build();

        deployment = deploymentRepository.save(deployment);

        // Update environment status
        env.setStatus(EnvironmentStatus.DEPLOYING);
        env.setLastActiveAt(LocalDateTime.now());
        environmentRepository.save(env);

        // Trigger GitHub Actions
        gitHubActionsClient.triggerDeployment(
                env.getNamespace(),
                request.getImageTag()
        );

        auditLogRepository.save(AuditLog.builder()
                .environmentId(environmentId)
                .action("DEPLOY_TRIGGERED")
                .performedBy(request.getTriggeredBy())
                .details("Image: " + request.getImageTag())
                .build());

        // Poll status in background and update
        final Deployment savedDeployment = deployment;
        final Environment savedEnv = env;

        CompletableFuture.runAsync(() -> {
            try {
                // Wait for pipeline to start
                Thread.sleep(10000);

                // Poll every 15 seconds for up to 5 minutes
                for (int i = 0; i < 20; i++) {
                    String status = gitHubActionsClient
                            .getLatestWorkflowStatus();
                    log.info("Workflow status: {}", status);

                    if (status.contains("completed:success")) {
                        savedDeployment.setStatus(DeploymentStatus.SUCCESS);
                        savedDeployment.setCompletedAt(LocalDateTime.now());
                        deploymentRepository.save(savedDeployment);

                        savedEnv.setStatus(EnvironmentStatus.RUNNING);
                        savedEnv.setLastActiveAt(LocalDateTime.now());
                        environmentRepository.save(savedEnv);

                        log.info("Deployment succeeded for {}",
                                savedEnv.getNamespace());
                        return;

                    } else if (status.contains("completed:failure")) {
                        savedDeployment.setStatus(DeploymentStatus.FAILED);
                        savedDeployment.setCompletedAt(LocalDateTime.now());
                        deploymentRepository.save(savedDeployment);

                        savedEnv.setStatus(EnvironmentStatus.RUNNING);
                        environmentRepository.save(savedEnv);

                        log.error("Deployment failed for {}",
                                savedEnv.getNamespace());
                        return;
                    }

                    Thread.sleep(15000);
                }
            } catch (Exception e) {
                log.error("Deployment polling error", e);
            }
        });

        return deployment;
    }
    public List<Deployment> getDeployments(String environmentId) {
        return deploymentRepository.findByEnvironmentIdOrderByTriggeredAtDesc(environmentId);
    }
}
