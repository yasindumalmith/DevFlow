package com.example.devflowapi.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {

    @Value("${devflow.aws.region}")
    private String region;

    @Value("${devflow.aws.cluster-name}")
    private String clusterName;

    @Bean
    public KubernetesClient kubernetesClient() {
        // Automatically picks up ~/.kube/config
        return new KubernetesClientBuilder().build();
    }
}