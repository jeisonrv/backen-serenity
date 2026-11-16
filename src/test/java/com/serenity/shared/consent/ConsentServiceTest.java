package com.serenity.shared.consent;

import com.serenity.shared.consent.infrastructure.persistence.ConsentLogJpaEntity;
import com.serenity.shared.consent.infrastructure.persistence.ConsentLogJpaRepository;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaEntity;
import com.serenity.usuario.infrastructure.adapter.out.persistence.UsuarioJpaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

    @Mock UsuarioJpaRepository usuarios;
    @Mock ConsentLogJpaRepository log;
    private ConsentService service;
    private UsuarioJpaEntity user;

    @BeforeEach
    void setUp() {
        service = new ConsentService(usuarios, log, new ConsentVersions("1.0", "1.0"));
        user = new UsuarioJpaEntity(12L, "persona", "p@example.com", null, 1, 0, 0, 0);
        lenient().when(usuarios.findById(12L)).thenReturn(Optional.of(user));
        lenient().when(usuarios.save(any(UsuarioJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void grantingSensitiveConsentUpdatesCurrentStateAndLogsGrant() {
        user.setTermsAcceptedAt(java.time.OffsetDateTime.now());
        user.setTermsVersion("1.0");
        ConsentStatus status = service.grantSensitiveConsent(12L);

        assertTrue(status.sensitiveDataAuthorized());
        assertEquals("1.0", user.getSensitiveConsentVersion());
        ArgumentCaptor<ConsentLogJpaEntity> entry = ArgumentCaptor.forClass(ConsentLogJpaEntity.class);
        verify(log).save(entry.capture());
        assertEquals(ConsentType.SENSITIVE_DATA, entry.getValue().getConsentType());
        assertEquals(ConsentAction.GRANTED, entry.getValue().getAction());
        assertEquals("1.0", entry.getValue().getPolicyVersion());
    }

    @Test
    void revokingSensitiveConsentClearsCurrentStateAndLogsRevocation() {
        user.setSensitiveConsentAt(java.time.OffsetDateTime.now());
        user.setSensitiveConsentVersion("1.0");

        ConsentStatus status = service.revokeSensitiveConsent(12L);

        assertFalse(status.sensitiveDataAuthorized());
        assertNull(user.getSensitiveConsentAt());
        assertNull(user.getSensitiveConsentVersion());
        ArgumentCaptor<ConsentLogJpaEntity> entry = ArgumentCaptor.forClass(ConsentLogJpaEntity.class);
        verify(log).save(entry.capture());
        assertEquals(ConsentAction.REVOKED, entry.getValue().getAction());
    }

    @Test
    void sensitiveConsentIsInvalidatedWhenPrivacyVersionChanges() {
        user.setSensitiveConsentAt(java.time.OffsetDateTime.now());
        user.setSensitiveConsentVersion("0.9");

        assertFalse(service.hasCurrentSensitiveConsent(12L));
        assertFalse(service.status(12L).sensitiveDataAuthorized());
        assertTrue(service.status(12L).sensitiveConsentPending());
    }
}
