package com.sadeem.multitenantsaas.controller;

import com.sadeem.multitenantsaas.model.AgentResult;
import com.sadeem.multitenantsaas.model.Workflow;
import com.sadeem.multitenantsaas.repository.AgentResultRepository;
import com.sadeem.multitenantsaas.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WorkflowGraphQLController {
    private final WorkflowService workflowService;
    @Autowired
    private AgentResultRepository agentResultRepository;

    public WorkflowGraphQLController(WorkflowService workflowService){
        this.workflowService = workflowService;
    }

    @QueryMapping
    public List<Workflow> workflowsByTenant(@Argument String tenantId) {
        return workflowService.getWorkflowsByTenant(tenantId);
    }

    @MutationMapping
    public Workflow createWorkflow(@Argument String name, @Argument String description, @Argument String tenantId, @Argument List<WorkflowService.StepInput> stepInputs) {
        return workflowService.createWorkflow(name, description, tenantId, stepInputs);
    }

    @MutationMapping
    public Workflow triggerWorkflow(@Argument String workflowId) {
        return workflowService.triggerWorkflow(workflowId);
    }

    @QueryMapping
    public List<AgentResult> agentResultsByWorkflow(@Argument String workflowId) {
        return agentResultRepository.findByWorkflowId(workflowId);
    }

    @QueryMapping
    public List<AgentResult> agentResultsByTenant(@Argument String tenantId) {
        return agentResultRepository.findByTenantId(tenantId);
    }
}
