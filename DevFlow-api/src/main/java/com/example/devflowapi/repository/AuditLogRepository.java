package com.example.devflowapi.repository;

import com.example.devflowapi.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    List<AuditLog> findByEnvironmentIdOrderByTimestampDesc(String environmentId);
}
