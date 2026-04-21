package com.sadeem.multitenantsaas.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowEvent {
    private String workflowId;
    private String workflowName;
    private String tenantId;
    private String eventType;
    private LocalDateTime timestamp = LocalDateTime.now();
}
