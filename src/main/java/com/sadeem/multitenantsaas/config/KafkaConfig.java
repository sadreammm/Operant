package com.sadeem.multitenantsaas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sadeem.multitenantsaas.event.WorkflowEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka // CRITICAL: This tells Spring to activate @KafkaListener annotations
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // 1. Unified ObjectMapper for BOTH Producer and Consumer
    @Bean
    public ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Force standard ISO strings (e.g., "2026-04-21T13:57:54") to prevent array-parsing errors
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    // =================== PRODUCER CONFIG ===================
    @Bean
    @SuppressWarnings("deprecation")
    public ProducerFactory<String, WorkflowEvent> producerFactory(ObjectMapper mapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new JsonSerializer<>(mapper));
    }

    @Bean
    public KafkaTemplate<String, WorkflowEvent> kafkaTemplate(ProducerFactory<String, WorkflowEvent> pf) {
        return new KafkaTemplate<>(pf);
    }

    // =================== CONSUMER CONFIG ===================
    @Bean
    public ConsumerFactory<String, WorkflowEvent> consumerFactory(ObjectMapper mapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "operant-backend-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<WorkflowEvent> deserializer = new JsonDeserializer<>(WorkflowEvent.class, mapper);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false); // Strictly map JSON directly to WorkflowEvent

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WorkflowEvent> kafkaListenerContainerFactory(ConsumerFactory<String, WorkflowEvent> cf) {
        ConcurrentKafkaListenerContainerFactory<String, WorkflowEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}