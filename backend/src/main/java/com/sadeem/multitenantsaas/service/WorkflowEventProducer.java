package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.event.WorkflowEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WorkflowEventProducer {
    private static final String TOPIC = "workflow.events";
    private final KafkaTemplate<String, WorkflowEvent> kafkaTemplate;

    public WorkflowEventProducer(KafkaTemplate<String, WorkflowEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishWorkflowStarted(WorkflowEvent event){
        event.setEventType("WORKFLOW_STARTED");
        kafkaTemplate.send(TOPIC, event.getWorkflowId(), event).whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("SUCCESSFULLY SENT to Kafka. Offset: " + result.getRecordMetadata().offset());
            } else {
                System.err.println("FAILED to send to Kafka: " + ex.getMessage());
            }
        });
        System.out.println("Published event: " + event.getEventType() + " for workflow: " + event.getWorkflowId());
    }
}
