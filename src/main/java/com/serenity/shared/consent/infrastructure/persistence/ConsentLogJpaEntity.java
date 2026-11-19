package com.serenity.shared.consent.infrastructure.persistence;

import com.serenity.shared.consent.ConsentAction;
import com.serenity.shared.consent.ConsentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "consent_log")
public class ConsentLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_type", nullable = false, length = 32)
    private ConsentType consentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ConsentAction action;

    @Column(name = "policy_version", nullable = false, length = 50)
    private String policyVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ConsentLogJpaEntity() { }

    public ConsentLogJpaEntity(
            Long userId,
            ConsentType consentType,
            ConsentAction action,
            String policyVersion,
            OffsetDateTime createdAt) {
        this.userId = userId;
        this.consentType = consentType;
        this.action = action;
        this.policyVersion = policyVersion;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public ConsentType getConsentType() { return consentType; }
    public ConsentAction getAction() { return action; }
    public String getPolicyVersion() { return policyVersion; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
