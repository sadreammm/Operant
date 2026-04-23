package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.model.Tenant;
import com.sadeem.multitenantsaas.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TenantProvisioningServiceTest {
    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private TenantProvisioningService tenantProvisioningService;

    @Test
    void createTenant_shouldCreateSchemaAndSaveTenant() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        Tenant savedTenant = new Tenant();
        savedTenant.setId("tenant-1");
        savedTenant.setOwnerEmail("test@test.com");
        savedTenant.setSchemaName("tenant_test");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        Tenant result = tenantProvisioningService.createTenant("test@test.com", "Test Tenant");

        assertNotNull(result);
        assertEquals("test@test.com", result.getOwnerEmail());
        assertEquals("tenant_test", result.getSchemaName());
        verify(entityManager, times(1)).createNativeQuery(contains("CREATE SCHEMA"));
    }

    @Test
    void createTenant_shouldSanitizeEmailForSchemaName() {
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        Tenant savedTenant = new Tenant();
        savedTenant.setSchemaName("tenant_test");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        Tenant result = tenantProvisioningService.createTenant("test@test.com", "Test");

        verify(entityManager, times(1)).createNativeQuery(contains("tenant_test"));
    }

}
