package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.agent.AgentService;
import com.sadeem.multitenantsaas.event.WorkflowEvent;
import com.sadeem.multitenantsaas.model.AgentResult;
import com.sadeem.multitenantsaas.model.Workflow;
import com.sadeem.multitenantsaas.model.WorkflowStep;
import com.sadeem.multitenantsaas.repository.WorkflowRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final WorkflowRepository workflowRepository;
    private final JavaMailSender mailSender;
    private final AgentService agentService;

    public NotificationService(WorkflowRepository workflowRepository, JavaMailSender mailSender, AgentService agentService){
        this.workflowRepository = workflowRepository;
        this.mailSender = mailSender;
        this.agentService = agentService;
    }

    @Transactional
    @KafkaListener(topics = "workflow.events", groupId = "operant-backend-consumer")
    public void handleWorkflowEvent(WorkflowEvent event){
        System.out.println("=== NOTIFICATION SERVICE ===");
        System.out.println("Received event: " + event.getEventType());
        System.out.println("Workflow: " + event.getWorkflowName());
        System.out.println("Tenant: " + event.getTenantId());
        System.out.println("Time: " + event.getTimestamp());
        System.out.println("============================");

        Workflow workflow = workflowRepository.findById(event.getWorkflowId()).orElse(null);

        if (workflow != null) {
            for (WorkflowStep step : workflow.getSteps()) {
                System.out.println("Executing " + step.getActionType() + " for Workflow: " + workflow.getName());

                if ("EMAIL".equals(step.getActionType())) {
                    sendEmail(step.getConfigData(), workflow.getName());
                } else if ("AI_AGENT".equals(step.getActionType())) {
                    AgentResult result = agentService.processWorkflowEvent(event, step.getConfigData());
                    System.out.println("=== AGENT COMPLETE ===");
                    System.out.println("Agent result: " + result.getAgentOutput());
                }
            }
        }
    }

    private void sendEmail(String configData, String workflowName) {
        String[] parts = configData.split(":");
        String recipient = parts[0];
        String body = parts.length > 1 ? parts[1] : "Workflow " + workflowName + " has been triggered.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject("Operant Workflow Alert: " + workflowName);
        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Email sent to " + recipient);
        }
        catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }

}
