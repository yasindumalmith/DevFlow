package com.example.devflowapi.metrics;

import com.example.devflowapi.model.EnvironmentStatus;
import com.example.devflowapi.repository.EnvironmentRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DevFlowMetrics {

    private final MeterRegistry meterRegistry;

    // Counters — track totals over time
    public void recordEnvironmentCreated() {
        meterRegistry.counter(
                "devflow.environments.created.total"
        ).increment();
    }

    public void recordEnvironmentDestroyed(String reason) {
        meterRegistry.counter(
                "devflow.environments.destroyed.total",
                "reason", reason
        ).increment();
    }

    public void recordDeploymentTriggered() {
        meterRegistry.counter(
                "devflow.deployments.triggered.total"
        ).increment();
    }

    public void recordDeploymentSuccess() {
        meterRegistry.counter(
                "devflow.deployments.success.total"
        ).increment();
    }

    public void recordDeploymentFailed() {
        meterRegistry.counter(
                "devflow.deployments.failed.total"
        ).increment();
    }

    // Gauge — tracks current live value
    public void registerActiveEnvironmentsGauge(
            EnvironmentRepository repo) {
        Gauge.builder("devflow.environments.active",
                        repo,
                        r -> r.findByStatus(
                                EnvironmentStatus.RUNNING).size())
                .description("Number of currently active environments")
                .register(meterRegistry);
    }

    // Timer — measures how long provisioning takes
    public Timer.Sample startProvisioningTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopProvisioningTimer(Timer.Sample sample) {
        sample.stop(meterRegistry.timer(
                "devflow.environment.provisioning.duration"
        ));
    }
}