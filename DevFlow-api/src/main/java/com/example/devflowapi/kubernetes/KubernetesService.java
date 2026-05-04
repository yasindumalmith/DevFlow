package com.example.devflowapi.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesService {

    private final KubernetesClient kubernetesClient;

    public boolean namespaceExists(String namespaceName) {
        return kubernetesClient.namespaces()
                .withName(namespaceName)
                .get() != null;
    }

    public Map<String, Object> getNamespaceStatus(String namespaceName) {
        Map<String, Object> status = new HashMap<>();

        // Get pods in namespace
        var pods = kubernetesClient.pods()
                .inNamespace(namespaceName)
                .list()
                .getItems();

        long runningPods = pods.stream()
                .filter(p -> "Running".equals(
                        p.getStatus().getPhase()))
                .count();

        long failedPods = pods.stream()
                .filter(p -> "Failed".equals(
                        p.getStatus().getPhase()))
                .count();

        int totalRestarts = pods.stream()
                .flatMap(p -> p.getStatus()
                        .getContainerStatuses().stream())
                .mapToInt(cs -> cs.getRestartCount())
                .sum();

        status.put("totalPods", pods.size());
        status.put("runningPods", runningPods);
        status.put("failedPods", failedPods);
        status.put("totalRestarts", totalRestarts);
        status.put("health", determineHealth(
                pods.size(), runningPods, failedPods, totalRestarts));

        return status;
    }

    private String determineHealth(int total, long running,
                                   long failed, int restarts) {
        if (total == 0) return "EMPTY";
        if (failed > 0 || restarts > 5) return "DEGRADED";
        if (running == total) return "HEALTHY";
        return "DEGRADED";
    }
}
