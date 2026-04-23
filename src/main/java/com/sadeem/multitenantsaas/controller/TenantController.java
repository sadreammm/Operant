package com.sadeem.multitenantsaas.controller;

import com.sadeem.multitenantsaas.model.Tenant;
import com.sadeem.multitenantsaas.repository.TenantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {
    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Tenant> getCurrentTenant(@AuthenticationPrincipal OAuth2User principal){
        if (principal == null) return ResponseEntity.status(401).build();
        String email = principal.getAttribute("email");
        return tenantRepository.findByOwnerEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
