package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.model.Tenant;
import com.sadeem.multitenantsaas.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TenantProvisioningService {
    private final TenantRepository tenantRepository;
    private final EntityManager entityManager;

    public TenantProvisioningService(TenantRepository tenantRepository, EntityManager entityManager){
        this.tenantRepository = tenantRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Tenant createTenant(String email, String name){
        String schemaName = "tenant_" + email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

        entityManager.createNativeQuery(
                "CREATE SCHEMA IF NOT EXISTS " + schemaName
        ).executeUpdate();

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setOwnerEmail(email);
        tenant.setSchemaName(schemaName);

        return tenantRepository.save(tenant);
    }
}
