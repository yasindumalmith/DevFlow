package com.example.devflowapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deployments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deployment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String environmentId;
    private String imageTag;
    private String triggeredBy;

    @Enumerated(EnumType.STRING)
    private DeploymentStatus status;

    private LocalDateTime triggeredAt;
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.triggeredAt = LocalDateTime.now();
        this.status = DeploymentStatus.TRIGGERED;
    }
}
