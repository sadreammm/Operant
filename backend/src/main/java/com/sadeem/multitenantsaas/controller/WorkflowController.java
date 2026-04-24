package com.sadeem.multitenantsaas.controller;

import com.sadeem.multitenantsaas.model.AgentResult;
import com.sadeem.multitenantsaas.model.Workflow;
import com.sadeem.multitenantsaas.repository.AgentResultRepository;
import com.sadeem.multitenantsaas.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService workflowService;
    private final AgentResultRepository agentResultRepository;

    public WorkflowController(WorkflowService workflowService, AgentResultRepository agentResultRepository) {
        this.workflowService = workflowService;
        this.agentResultRepository = agentResultRepository;
    }

    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestBody WorkflowRequest request){
        Workflow workflow = workflowService.createWorkflow(
                request.name(), request.description(), request.tenantId(), request.steps()
        );
        return ResponseEntity.ok(workflow);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<Workflow>> getWorkflows(@PathVariable String tenantId){
        return ResponseEntity.ok(workflowService.getWorkflowsByTenant(tenantId));
    }

    @PostMapping("/{workflowId}/trigger")
    public ResponseEntity<Workflow> triggerWorkflow(@PathVariable String workflowId){
        return ResponseEntity.ok(workflowService.triggerWorkflow(workflowId));
    }

    @GetMapping("/{workflowId}/results")
    public ResponseEntity<List<AgentResult>> getAgentResults(@PathVariable String workflowId) {
        return ResponseEntity.ok(agentResultRepository.findByWorkflowId(workflowId));
    }

    public record WorkflowRequest(String name, String description, String tenantId, List<WorkflowService.StepInput> steps){}
}
