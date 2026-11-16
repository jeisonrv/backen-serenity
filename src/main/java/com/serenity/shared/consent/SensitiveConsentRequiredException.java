package com.serenity.shared.consent;

public class SensitiveConsentRequiredException extends RuntimeException {
    public SensitiveConsentRequiredException() {
        super("Debes autorizar el tratamiento de datos sensibles para usar esta función");
    }
}
