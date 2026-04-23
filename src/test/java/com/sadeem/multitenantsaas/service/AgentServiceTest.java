package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.agent.AgentService;
import com.sadeem.multitenantsaas.agent.AgentToolRegistry;
import com.sadeem.multitenantsaas.agent.GeminiClient;
import com.sadeem.multitenantsaas.event.WorkflowEvent;
import com.sadeem.multitenantsaas.model.AgentResult;
import com.sadeem.multitenantsaas.repository.AgentResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class AgentServiceTest {
    @Mock
    private GeminiClient geminiClient;

    @Mock
    private AgentToolRegistry toolRegistry;

    @Mock
    private AgentResultRepository agentResultRepository;

    @InjectMocks
    private AgentService agentService;

    private WorkflowEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new WorkflowEvent();
        testEvent.setWorkflowId("workflow-1");
        testEvent.setWorkflowName("Test Workflow");
        testEvent.setTenantId("tenant-1");
        testEvent.setEventType("WORKFLOW_STARTED");
        testEvent.setTimestamp(LocalDateTime.now());
    }

    @Test
    void processWorkflowEvent_shouldCallLLMAndStoreResult() {
        when(toolRegistry.getTenantContext("tenant-1")).thenReturn("Tenant: Test | Email: test@test.com");
        when(toolRegistry.getWorkflowSummary("tenant-1")).thenReturn("- Test Workflow (RUNNING)");
        when(geminiClient.generateResponse(any(String.class))).thenReturn("Agent Analsysis complete");

        AgentResult mockResult = new AgentResult();
        mockResult.setId("result-1");
        mockResult.setWorkflowId("workflow-1");
        mockResult.setAgentOutput("Agent Analsysis complete");
        mockResult.setStatus(AgentResult.AgentStatus.SUCCESS);
        when(agentResultRepository.save(any(AgentResult.class))).thenReturn(mockResult);

        AgentResult result = agentService.processWorkflowEvent(testEvent, "Step 1: Agent Analysis");

        assertNotNull(result);
        assertEquals("Agent Analsysis complete", result.getAgentOutput());
        assertEquals(AgentResult.AgentStatus.SUCCESS, result.getStatus());
        verify(geminiClient, times(1)).generateResponse(any(String.class));
        verify(agentResultRepository, times(1)).save(any(AgentResult.class));
    }

    @Test
    void porcessWorkflowEvent_shouldIncludeTenantContextInPrompt() {
        when(toolRegistry.getTenantContext("tenant-1")).thenReturn("Tenant: Test Corp");
        when(toolRegistry.getWorkflowSummary("tenant-1")).thenReturn("- Workflow 1 (ACTIVE)");
        when(geminiClient.generateResponse(any(String.class))).thenReturn("Done.");

        AgentResult mockResult = new AgentResult();
        mockResult.setStatus(AgentResult.AgentStatus.SUCCESS);
        when(agentResultRepository.save(any(AgentResult.class))).thenReturn(mockResult);

        agentService.processWorkflowEvent(testEvent, "Step 1: Agent Analysis");

        verify(toolRegistry, times(1)).getTenantContext("tenant-1");
        verify(toolRegistry, times(1)).getWorkflowSummary("tenant-1");
    }

}
