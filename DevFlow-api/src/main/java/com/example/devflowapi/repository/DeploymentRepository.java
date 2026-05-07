package com.example.devflowapi.repository;

import com.example.devflowapi.model.Deployment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, String> {
    List<Deployment> findByEnvironmentIdOrderByTriggeredAtDesc(String environmentId);
}
