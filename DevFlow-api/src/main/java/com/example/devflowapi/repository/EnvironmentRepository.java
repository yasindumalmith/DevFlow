package com.example.devflowapi.repository;

import com.example.devflowapi.model.Environment;
import com.example.devflowapi.model.EnvironmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EnvironmentRepository extends JpaRepository<Environment, String> {
    List<Environment> findByStatus(EnvironmentStatus status);
    List<Environment> findByOwnerEmail(String email);

    List<Environment> findByStatusAndLastActiveAtBefore(EnvironmentStatus status, LocalDateTime lastActiveAtBefore);
}
