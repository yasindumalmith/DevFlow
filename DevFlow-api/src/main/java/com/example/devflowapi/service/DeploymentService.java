package com.example.devflowapi.service;

import com.example.devflowapi.dto.TriggerDeploymentRequest;
import com.example.devflowapi.model.Deployment;

import java.util.List;

public interface DeploymentService {
    Deployment triggerDeployment(String environmentId, TriggerDeploymentRequest request);
    List<Deployment> getDeployments(String environmentId);
}
