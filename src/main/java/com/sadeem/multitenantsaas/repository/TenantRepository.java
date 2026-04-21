package com.sadeem.multitenantsaas.repository;
import com.sadeem.multitenantsaas.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByOwnerEmail(String email);
    Optional<Tenant> findBySchemaName(String schemaName);
};
