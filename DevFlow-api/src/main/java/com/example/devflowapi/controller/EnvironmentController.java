package com.example.devflowapi.controller;

import com.example.devflowapi.dto.CreateEnvironmentRequest;
import com.example.devflowapi.dto.EnvironmentResponse;
import com.example.devflowapi.service.EnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/environments")
@RequiredArgsConstructor
@Slf4j
public class EnvironmentController {
    private final EnvironmentService environmentService;

    @PostMapping
    public ResponseEntity<EnvironmentResponse> create(
            @Valid @RequestBody CreateEnvironmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(environmentService.createEnvironment(request));
    }

    @GetMapping
    public ResponseEntity<List<EnvironmentResponse>> getAll() {
        return ResponseEntity.ok(environmentService.getAllEnvironments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentResponse> getOne(@PathVariable String id) {
        return ResponseEntity.ok(environmentService.getEnvironment(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestParam String deletedBy) {
        environmentService.deleteEnvironment(id, deletedBy);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}/health")
    public ResponseEntity<Map<String, Object>> getHealth(@PathVariable String id) {
        return ResponseEntity.ok(environmentService.getEnvironmentHealth(id));
    }
}
