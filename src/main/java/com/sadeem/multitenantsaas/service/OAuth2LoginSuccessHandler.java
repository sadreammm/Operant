package com.sadeem.multitenantsaas.service;

import com.sadeem.multitenantsaas.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final TenantRepository tenantRepository;
    private final TenantProvisioningService provisioningService;

    public OAuth2LoginSuccessHandler(TenantRepository tenantRepository, TenantProvisioningService provisioningService){
        this.tenantRepository = tenantRepository;
        this.provisioningService = provisioningService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException{

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        tenantRepository.findByOwnerEmail(email)
                .orElseGet(() -> provisioningService.createTenant(email, name));

        response.sendRedirect("http://localhost:4200/dashboard");
    }
}
