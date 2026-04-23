package com.sadeem.multitenantsaas.repository;

import com.sadeem.multitenantsaas.model.AgentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentResultRepository extends JpaRepository<AgentResult, String> {
    List<AgentResult> findByWorkflowId(String workflowId);
    List<AgentResult> findByTenantId(String tenantId);
}
