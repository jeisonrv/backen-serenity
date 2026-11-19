package com.serenity.shared.consent;

import com.serenity.shared.consent.infrastructure.persistence.ConsentLogJpaEntity;
import com.serenity.shared.consent.infrastructure.persistence.ConsentLogJpaRepository;
import com.serenity.shared.exception.ResourceNotFoundException;
import com.serenity.usuario.domain.model.Usuario;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaEntity;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsentService {

    private final UsuarioJpaRepository usuarios;
    private final ConsentLogJpaRepository consentLog;
    private final ConsentVersions versions;

    public ConsentService(
            UsuarioJpaRepository usuarios,
            ConsentLogJpaRepository consentLog,
            ConsentVersions versions) {
        this.usuarios = usuarios;
        this.consentLog = consentLog;
        this.versions = versions;
    }

    public void prepareRegistration(Usuario usuario, boolean acceptSensitiveData) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        usuario.setTermsAcceptedAt(now);
        usuario.setTermsVersion(versions.termsVersion());
        if (acceptSensitiveData) {
            usuario.setSensitiveConsentAt(now);
            usuario.setSensitiveConsentVersion(versions.privacyVersion());
        }
    }

    @Transactional
    public void recordRegistration(Long userId, boolean acceptSensitiveData) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        consentLog.save(new ConsentLogJpaEntity(
                userId, ConsentType.TERMS, ConsentAction.GRANTED, versions.termsVersion(), now));
        if (acceptSensitiveData) {
            consentLog.save(new ConsentLogJpaEntity(
                    userId, ConsentType.SENSITIVE_DATA, ConsentAction.GRANTED, versions.privacyVersion(), now));
        }
    }

    @Transactional(readOnly = true)
    public ConsentStatus status(Long userId) {
        UsuarioJpaEntity user = usuario(userId);
        boolean termsAccepted = user.getTermsAcceptedAt() != null;
        boolean sensitiveAuthorized = isSensitiveConsentCurrent(user);
        return new ConsentStatus(
                termsAccepted,
                user.getTermsVersion(),
                user.getTermsAcceptedAt(),
                !termsAccepted || !versions.termsVersion().equals(user.getTermsVersion()),
                sensitiveAuthorized,
                user.getSensitiveConsentVersion(),
                user.getSensitiveConsentAt(),
                !sensitiveAuthorized,
                versions.termsVersion(),
                versions.privacyVersion());
    }

    @Transactional
    public ConsentStatus acceptTerms(Long userId) {
        UsuarioJpaEntity user = usuario(userId);
        if (user.getTermsAcceptedAt() == null
                || !versions.termsVersion().equals(user.getTermsVersion())) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            user.setTermsAcceptedAt(now);
            user.setTermsVersion(versions.termsVersion());
            usuarios.save(user);
            consentLog.save(new ConsentLogJpaEntity(
                    userId, ConsentType.TERMS, ConsentAction.GRANTED, versions.termsVersion(), now));
        }
        return status(userId);
    }

    @Transactional
    public ConsentStatus grantSensitiveConsent(Long userId) {
        UsuarioJpaEntity user = usuario(userId);
        if (user.getTermsAcceptedAt() == null
                || !versions.termsVersion().equals(user.getTermsVersion())) {
            throw new IllegalArgumentException(
                    "Primero debes aceptar los términos vigentes y confirmar que fuiste informado de la Política de Tratamiento de Datos"
            );
        }
        if (!isSensitiveConsentCurrent(user)) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            user.setSensitiveConsentAt(now);
            user.setSensitiveConsentVersion(versions.privacyVersion());
            usuarios.save(user);
            consentLog.save(new ConsentLogJpaEntity(
                    userId, ConsentType.SENSITIVE_DATA, ConsentAction.GRANTED, versions.privacyVersion(), now));
        }
        return status(userId);
    }

    @Transactional
    public ConsentStatus revokeSensitiveConsent(Long userId) {
        UsuarioJpaEntity user = usuario(userId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        user.setSensitiveConsentAt(null);
        user.setSensitiveConsentVersion(null);
        usuarios.save(user);
        consentLog.save(new ConsentLogJpaEntity(
                userId, ConsentType.SENSITIVE_DATA, ConsentAction.REVOKED, versions.privacyVersion(), now));
        // TODO: la supresión de datos se gestiona mediante solicitud del titular (arts. 8 y 15 de la Ley 1581 de 2012).
        return status(userId);
    }

    @Transactional(readOnly = true)
    public boolean hasCurrentSensitiveConsent(Long userId) {
        return usuarios.findById(userId).map(this::isSensitiveConsentCurrent).orElse(false);
    }

    public ConsentPoliciesResponse currentVersions() {
        return new ConsentPoliciesResponse(versions.termsVersion(), versions.privacyVersion());
    }

    private boolean isSensitiveConsentCurrent(UsuarioJpaEntity user) {
        return user.getSensitiveConsentAt() != null
                && versions.privacyVersion().equals(user.getSensitiveConsentVersion());
    }

    private UsuarioJpaEntity usuario(Long userId) {
        return usuarios.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
