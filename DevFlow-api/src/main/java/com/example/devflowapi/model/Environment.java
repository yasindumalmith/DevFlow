package com.example.devflowapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "environments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Environment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String ownerEmail;

    @Enumerated(EnumType.STRING)
    private EnvironmentStatus status;

    private String namespace;

    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime destroyedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.status = EnvironmentStatus.PROVISIONING;
    }
}