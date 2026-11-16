package com.serenity.shared.consent;

import java.time.OffsetDateTime;

public record ConsentStatus(
        boolean termsAccepted,
        String termsVersion,
        OffsetDateTime termsAcceptedAt,
        boolean termsAcceptancePending,
        boolean sensitiveDataAuthorized,
        String sensitiveConsentVersion,
        OffsetDateTime sensitiveConsentAt,
        boolean sensitiveConsentPending,
        String currentTermsVersion,
        String currentPrivacyVersion
) { }
