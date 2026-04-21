package com.sadeem.multitenantsaas.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_results", schema = "public")
@Data
public class AgentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String workflowId;

    @Column(nullable = false)
    private String tenantId;

    @Column(columnDefinition = "TEXT")
    private String agentOutput;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;

    private LocalDateTime processedAt = LocalDateTime.now();

    public enum AgentStatus { SUCCESS, FAILED }
}
