package com.serenity.shared.consent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsentVersions {

    private final String termsVersion;
    private final String privacyVersion;

    public ConsentVersions(
            @Value("${terms.version:1.0}") String termsVersion,
            @Value("${privacy.version:1.0}") String privacyVersion) {
        this.termsVersion = termsVersion;
        this.privacyVersion = privacyVersion;
    }

    public String termsVersion() { return termsVersion; }
    public String privacyVersion() { return privacyVersion; }
}
