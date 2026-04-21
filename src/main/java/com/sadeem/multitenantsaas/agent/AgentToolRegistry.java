package com.sadeem.multitenantsaas.agent;

import com.sadeem.multitenantsaas.model.Tenant;
import com.sadeem.multitenantsaas.repository.TenantRepository;
import com.sadeem.multitenantsaas.repository.WorkflowRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class AgentToolRegistry {
    private final TenantRepository tenantRepository;
    private final WorkflowRepository workflowRepository;
    private final JavaMailSender mailSender;

    public AgentToolRegistry(TenantRepository tenantRepository, WorkflowRepository workflowRepository, JavaMailSender mailSender) {
        this.tenantRepository = tenantRepository;
        this.workflowRepository = workflowRepository;
        this.mailSender = mailSender;
    }

    // READ ACCESS TOOLS
    // Get Tenant Context
    public String getTenantContext(String tenantId) {
        return tenantRepository.findById(tenantId)
                .map(tenant -> String.format(
                        "Tenant: %s | Email: %s | Status: %s | Schema: %s",
                        tenant.getName(),
                        tenant.getOwnerEmail(),
                        tenant.getStatus(),
                        tenant.getSchemaName()
                ))
                .orElse("Tenant Not Found");
    }

    // Get Workflow History for Tenant
    public String getWorkflowSummary(String tenantId) {
        var workflows = workflowRepository.findByTenantId(tenantId);
        if (workflows.isEmpty()) return "No workflows found for this tenant.";

        StringBuilder sb = new StringBuilder();
        workflows.forEach(w -> sb.append(String.format(
                "- %s (%s): %s\n", w.getName(), w.getStatus(), w.getDescription()
        )));
        return sb.toString();
    }

    // WRITE ACCESS TOOLS
    public String suspendTenant(String tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant != null) {
            tenant.setStatus(Tenant.TenantStatus.SUSPENDED);
            tenantRepository.save(tenant);
            return "SUCCESS: Tenant " + tenantId + " suspended.";
        }
        return "FAILED: Tenant not found.";
    }

    public String sendEmail(String email, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("admin@operant.com");
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent to " + email);
            return "SUCCESS: Email sent to " + email;
        } catch(Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            return "FAILED: Could not send email.";
        }
    }
}
