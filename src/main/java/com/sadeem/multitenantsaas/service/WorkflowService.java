package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.event.WorkflowEvent;
import com.sadeem.multitenantsaas.model.Workflow;
import com.sadeem.multitenantsaas.model.WorkflowStep;
import com.sadeem.multitenantsaas.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowEventProducer workflowEventProducer;

    public WorkflowService(WorkflowRepository workflowRepository, WorkflowEventProducer workflowEventProducer){
        this.workflowRepository = workflowRepository;
        this.workflowEventProducer = workflowEventProducer;
    }

    @Transactional
    public Workflow createWorkflow(String name, String description, String tenantId, List<StepInput> stepInputs){
        Workflow workflow = new Workflow();
        workflow.setName(name);
        workflow.setDescription(description);
        workflow.setTenantId(tenantId);

        if (stepInputs != null) {
            List<WorkflowStep> steps = stepInputs.stream().map(input -> {
                WorkflowStep step = new WorkflowStep();
                step.setWorkflow(workflow);
                step.setActionType(input.actionType());
                step.setConfigData(input.configData());
                step.setStepOrder(input.stepOrder());
                return step;
            }).collect(Collectors.toList());
            workflow.setSteps(steps);
        }
        return workflowRepository.save(workflow);
    }

    public List<Workflow> getWorkflowsByTenant(String tenantId){
        return workflowRepository.findByTenantId(tenantId);
    }

    public Workflow triggerWorkflow(String workflowId){
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        workflow.setStatus(Workflow.WorkflowStatus.RUNNING);
        workflowRepository.save(workflow);

        WorkflowEvent event = new WorkflowEvent();
        event.setWorkflowId(workflow.getId());
        event.setWorkflowName(workflow.getName());
        event.setTenantId(workflow.getTenantId());
        workflowEventProducer.publishWorkflowStarted(event);

        return workflow;
    }

    public record StepInput(String actionType, String configData, int stepOrder){}
}
