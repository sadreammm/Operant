package com.sadeem.multitenantsaas.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", schema = "public")
@Data
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String schemaName;

    @Column(nullable = false, unique = true)
    private String ownerEmail;

    @Enumerated(EnumType.STRING)
    private TenantStatus status = TenantStatus.ACTIVE;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TenantStatus { ACTIVE, SUSPENDED}
}
