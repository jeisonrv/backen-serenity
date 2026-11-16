package com.serenity.shared.consent;

import com.serenity.shared.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class SensitiveConsentInterceptor implements HandlerInterceptor {

    private final ConsentService consentService;

    public SensitiveConsentInterceptor(ConsentService consentService) {
        this.consentService = consentService;
    }

    @Override
    public boolean preHandle(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Object handler) {
        if (!(handler instanceof HandlerMethod method) || !requiresConsent(method)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)
                || !consentService.hasCurrentSensitiveConsent(user.id())) {
            throw new SensitiveConsentRequiredException();
        }
        return true;
    }

    private boolean requiresConsent(HandlerMethod handler) {
        return handler.hasMethodAnnotation(RequireSensitiveConsent.class)
                || handler.getBeanType().isAnnotationPresent(RequireSensitiveConsent.class);
    }
}
