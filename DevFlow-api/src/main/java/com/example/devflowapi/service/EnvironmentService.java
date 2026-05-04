package com.example.devflowapi.service;

import com.example.devflowapi.dto.CreateEnvironmentRequest;
import com.example.devflowapi.dto.EnvironmentResponse;

import java.util.List;
import java.util.Map;

public interface EnvironmentService {
    EnvironmentResponse createEnvironment(CreateEnvironmentRequest request);
    List<EnvironmentResponse> getAllEnvironments();
    EnvironmentResponse getEnvironment(String id);
    void deleteEnvironment(String id, String deletedBy);
    Map<String, Object> getEnvironmentHealth(String id);

}
