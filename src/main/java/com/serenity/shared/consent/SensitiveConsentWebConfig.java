package com.serenity.shared.consent;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SensitiveConsentWebConfig implements WebMvcConfigurer {

    private final ConsentService consentService;

    public SensitiveConsentWebConfig(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SensitiveConsentInterceptor(consentService));
    }
}
