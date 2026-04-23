package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.event.WorkflowEvent;
import com.sadeem.multitenantsaas.model.Workflow;
import com.sadeem.multitenantsaas.repository.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkflowServiceTest {
    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowEventProducer workflowEventProducer;

    @InjectMocks
    private WorkflowService workflowService;

    private Workflow testWorkflow;

    @BeforeEach
    void setUp(){
        testWorkflow = new Workflow();
        testWorkflow.setId("workflow-1");
        testWorkflow.setName("Test Workflow");
        testWorkflow.setDescription("This is a test workflow");
        testWorkflow.setTenantId("tenant-1");
        testWorkflow.setStatus(Workflow.WorkflowStatus.ACTIVE);
    }

    @Test
    void createWorkflow_shouldSaveAndReturnWorkflow() {
        when(workflowRepository.save(any(Workflow.class))).thenReturn(testWorkflow);

        Workflow result = workflowService.createWorkflow(
                "Test Workflow", "This is a test workflow", "tenant-1", List.of()
        );

        assertNotNull(result);
        assertEquals("Test Workflow", result.getName());
        assertEquals("tenant-1", result.getTenantId());
        verify(workflowRepository, times(1)).save(any(Workflow.class));
    }

    @Test
    void triggerWorkflow_shouldPublishKafkaEventAndSetStatusRunning() {
        when(workflowRepository.findById("workflow-1")).thenReturn(Optional.of(testWorkflow));
        when(workflowRepository.save(any(Workflow.class))).thenReturn(testWorkflow);
        doNothing().when(workflowEventProducer).publishWorkflowStarted(any(WorkflowEvent.class));

        Workflow result = workflowService.triggerWorkflow("workflow-1");

        assertEquals(Workflow.WorkflowStatus.RUNNING, result.getStatus());
        verify(workflowEventProducer, times(1)).publishWorkflowStarted(any(WorkflowEvent.class));
    }

    @Test
    void triggerWorfkflow_shouldThrowWhenWorkflowNotFound() {
        when(workflowRepository.findById("invalid-id")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                workflowService.triggerWorkflow("invalid-id")
        );

        assertEquals("Workflow not found", ex.getMessage());
    }

    @Test
    void getWorkflowsByTenant_shouldReturnTenantWorkflows() {
        when(workflowRepository.findByTenantId("tenant-1")).thenReturn(List.of(testWorkflow));

        List<Workflow> result = workflowService.getWorkflowsByTenant("tenant-1");

        assertEquals(1, result.size());
        assertEquals("Test Workflow", result.get(0).getName());
    }
}
