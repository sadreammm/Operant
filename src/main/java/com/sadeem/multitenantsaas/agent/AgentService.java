package com.sadeem.multitenantsaas.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadeem.multitenantsaas.event.WorkflowEvent;
import com.sadeem.multitenantsaas.model.AgentResult;
import com.sadeem.multitenantsaas.repository.AgentResultRepository;
import org.springframework.stereotype.Service;

@Service
public class AgentService {
    private final GeminiClient geminiClient;
    private final AgentToolRegistry toolRegistry;
    private final AgentResultRepository agentResultRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentService(GeminiClient geminiClient, AgentToolRegistry toolRegistry, AgentResultRepository agentResultRepository) {
        this.geminiClient = geminiClient;
        this.toolRegistry = toolRegistry;
        this.agentResultRepository = agentResultRepository;
    }

    public AgentResult processWorkflowEvent(WorkflowEvent event, String stepInstructions) {
        System.out.println("Agent pickup up workflow: " + event.getWorkflowId());

        // Tools to gather context
        String tenantContext = toolRegistry.getTenantContext(event.getTenantId());
        String workflowHistory = toolRegistry.getWorkflowSummary(event.getTenantId());

        // Build prompt
        String prompt = buildAgentPrompt(event, tenantContext, workflowHistory, stepInstructions);

        // Call LLM
        String agentOutput = geminiClient.generateResponse(prompt);

        String cleanedOutput = agentOutput
                .replace("```json", "")
                .replace("```", "")
                .trim();

        String executionLog = "";

        try {
            JsonNode actionPlan = objectMapper.readTree(cleanedOutput);
            String toolToUse = actionPlan.path("toolToUse").asText();

            JsonNode arguments = actionPlan.path("arguments");

            System.out.println("Tool to use: " + toolToUse);

            switch (toolToUse) {
                case "SEND_EMAIL":
                    String email = arguments.path("email").asText();
                    String subject = arguments.path("subject").asText();
                    String body = arguments.path("body").asText();
                    executionLog = toolRegistry.sendEmail(email, subject, body);
                    break;
                case "SUSPEND_TENANT":
                    String tenantId = arguments.path("tenantId").asText();
                    executionLog = toolRegistry.suspendTenant(tenantId);
                    break;
                case "NONE":
                default:
                    executionLog = "AI Analysis: " + arguments.path("analysis").asText();
                    break;
            }
        } catch (Exception e) {
            executionLog = "Agent Error Parsing JSON: " + e.getMessage() + " | Raw Output: " + agentOutput;
        }

        // Store Result
        AgentResult result = new AgentResult();
        result.setWorkflowId(event.getWorkflowId());
        result.setTenantId(event.getTenantId());
        result.setAgentOutput("ACTION TAKEN: " + executionLog);
        result.setStatus(AgentResult.AgentStatus.SUCCESS);

        AgentResult saved = agentResultRepository.save(result);
        System.out.println("Agent result saved: " + saved.getId());

        return saved;
    }

    private String buildAgentPrompt(WorkflowEvent event, String tenantContext, String workflowHistory, String stepInstructions) {
        return String.format("""
            You are an autonomous AI agent for a SaaS workflow automation platform.
            You have the ability to execute system commands.
            
            WORKFLOW DETAILS:
            - Name: %s
            - ID: %s
            - Triggered at: %s
            
            TENANT CONTEXT:
            %s
            
            TENANT WORKFLOW HISTORY:
            %s
            
            YOUR INSTRUCTIONS (Execute these exactly based on the context above):
            %s
            
            AVAILABLE TOOLS:
            1. SEND_EMAIL - Sends an email to a user. Requires arguments: 'email', 'subject', 'body'.
            2. SUSPEND_TENANT - Locks a tenant's account. Requires arguments: 'tenantId', 'reason'.
            3. NONE - Take no action. Requires arguments: 'analysis'.
    
            You MUST respond ONLY with a raw JSON object matching this exact schema. Do not include markdown code blocks (```json).
            {
              "toolToUse": "SEND_EMAIL | SUSPEND_TENANT | NONE",
              "arguments": {
                 // Provide the required keys and values based on the tool you chose
              }
            }
            
            Keep your response concise and professional.
            """,
                event.getWorkflowName(),
                event.getWorkflowId(),
                event.getTimestamp(),
                tenantContext,
                workflowHistory,
                stepInstructions
        );
    }
}
