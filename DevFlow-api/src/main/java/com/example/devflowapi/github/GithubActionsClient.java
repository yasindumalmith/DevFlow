package com.example.devflowapi.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class GithubActionsClient {
    @Value("${devflow.github.token}")
    private String githubToken;

    @Value("${devflow.github.owner}")
    private String owner;

    @Value("${devflow.github.sample-app-repo}")
    private String repo;

    @Value("${devflow.github.workflow-id}")
    private String workflowId;

    private final RestTemplate restTemplate;

    public GithubActionsClient() {
        this.restTemplate = new RestTemplate();
    }

    public void triggerDeployment(String namespaceName,
                                  String imageTag) {
        assertConfigured();

        String url = String.format(
                "https://api.github.com/repos/%s/%s/actions/workflows/%s/dispatches",
                owner, repo, workflowId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "ref", "main",
                "inputs", Map.of(
                        "environment_name", namespaceName,
                        "image_tag", imageTag
                )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("Triggered deployment for namespace: {} tag: {}",
                    namespaceName, imageTag);
        } catch (Exception e) {
            log.error("Failed to trigger GitHub Actions", e);
            throw new RuntimeException("Pipeline trigger failed: "
                    + e.getMessage());
        }
    }

    public String getLatestWorkflowStatus() {
        assertConfigured();

        String url = String.format(
                "https://api.github.com/repos/%s/%s/actions/workflows/%s/runs?per_page=1",
                owner, repo, workflowId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Map.class
            );

            Map body = response.getBody();
            if (body != null) {
                var runs = (java.util.List) body.get("workflow_runs");
                if (runs != null && !runs.isEmpty()) {
                    Map latestRun = (Map) runs.get(0);
                    return (String) latestRun.get("status")
                            + ":" + latestRun.get("conclusion");
                }
            }
        } catch (Exception e) {
            log.error("Failed to get workflow status", e);
        }

        return "unknown";
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(githubToken)) {
            throw new IllegalStateException(
                    "GitHub Actions is not configured. Set the GITHUB_TOKEN environment variable."
            );
        }
        if (!StringUtils.hasText(owner)
                || !StringUtils.hasText(repo)
                || !StringUtils.hasText(workflowId)) {
            throw new IllegalStateException(
                    "GitHub Actions is not configured. Check devflow.github.owner, "
                            + "devflow.github.sample-app-repo, and devflow.github.workflow-id."
            );
        }
    }
}
