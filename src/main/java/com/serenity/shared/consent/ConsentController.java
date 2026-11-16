package com.serenity.shared.consent;

import com.serenity.shared.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/consents")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping("/me")
    public ConsentStatus status(@AuthenticationPrincipal AuthenticatedUser user) {
        return consentService.status(user.id());
    }

    @PostMapping("/terms")
    public ConsentStatus acceptTerms(@AuthenticationPrincipal AuthenticatedUser user) {
        return consentService.acceptTerms(user.id());
    }

    @PostMapping("/sensitive")
    public ConsentStatus grantSensitive(@AuthenticationPrincipal AuthenticatedUser user) {
        return consentService.grantSensitiveConsent(user.id());
    }

    @DeleteMapping("/sensitive")
    public ConsentStatus revokeSensitive(@AuthenticationPrincipal AuthenticatedUser user) {
        return consentService.revokeSensitiveConsent(user.id());
    }

    @GetMapping("/versions")
    @ResponseStatus(HttpStatus.OK)
    public ConsentPoliciesResponse versions() {
        return consentService.currentVersions();
    }
}
