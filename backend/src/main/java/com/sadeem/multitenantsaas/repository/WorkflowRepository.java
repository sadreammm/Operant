package com.sadeem.multitenantsaas.repository;

import com.sadeem.multitenantsaas.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    List<Workflow> findByTenantId(String tenantId);
}
