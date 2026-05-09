package com.example.devflowapi.scheduler;

import com.example.devflowapi.model.Environment;
import com.example.devflowapi.model.EnvironmentStatus;
import com.example.devflowapi.repository.EnvironmentRepository;
import com.example.devflowapi.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnvironmentCleanupScheduler {

    private final EnvironmentRepository environmentRepository;
    private final EnvironmentService environmentService;

    // Runs every 5 minutes
    @Scheduled(fixedDelay = 300000)
    public void destroyIdleEnvironments() {
        log.info("Running idle environment cleanup check...");

        LocalDateTime idleCutoff = LocalDateTime.now().minusMinutes(30);

        // Find environments that are RUNNING but haven't been
        // active for 30 minutes
        List<Environment> idleEnvironments = environmentRepository
                .findByStatusAndLastActiveAtBefore(
                        EnvironmentStatus.RUNNING,
                        idleCutoff
                );

        if (idleEnvironments.isEmpty()) {
            log.info("No idle environments found");
            return;
        }

        log.info("Found {} idle environments to destroy",
                idleEnvironments.size());

        for (Environment env : idleEnvironments) {
            log.info("Auto-destroying idle environment: {} " +
                            "(last active: {})",
                    env.getName(), env.getLastActiveAt());

            try {
                environmentService.deleteEnvironment(
                        env.getId(),
                        "system-auto-cleanup"
                );
            } catch (Exception e) {
                log.error("Failed to auto-destroy environment: {}",
                        env.getName(), e);
            }
        }
    }
}