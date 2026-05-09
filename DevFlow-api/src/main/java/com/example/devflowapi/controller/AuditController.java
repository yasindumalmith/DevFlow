package com.example.devflowapi.controller;

import com.example.devflowapi.model.AuditLog;
import com.example.devflowapi.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAll() {
        return ResponseEntity.ok(
                auditLogRepository.findAll());
    }

    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<AuditLog>> getByEnvironment(@PathVariable String environmentId) {
        return ResponseEntity.ok(auditLogRepository.findByEnvironmentIdOrderByTimestampDesc(environmentId));
    }
}
