package com.sadeem.multitenantsaas.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "workflow_steps", schema = "public")
@Data
public class WorkflowStep {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id")
    @JsonIgnore
    private Workflow workflow;

    private String actionType;

    @Column(columnDefinition = "TEXT")
    private String configData;

    private int stepOrder;
}
