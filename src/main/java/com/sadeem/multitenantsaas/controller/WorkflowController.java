package com.sadeem.multitenantsaas.controller;

import com.sadeem.multitenantsaas.model.Workflow;
import com.sadeem.multitenantsaas.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
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

    public record WorkflowRequest(String name, String description, String tenantId, List<WorkflowService.StepInput> steps){}
}
