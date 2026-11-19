ALTER TABLE IF EXISTS usuarios
    ADD COLUMN IF NOT EXISTS terms_accepted_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS terms_version VARCHAR(50),
    ADD COLUMN IF NOT EXISTS sensitive_consent_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS sensitive_consent_version VARCHAR(50);

CREATE TABLE IF NOT EXISTS consent_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    consent_type VARCHAR(32) NOT NULL CHECK (consent_type IN ('TERMS', 'SENSITIVE_DATA')),
    action VARCHAR(16) NOT NULL CHECK (action IN ('GRANTED', 'REVOKED')),
    policy_version VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_consent_log_user_created
    ON consent_log (user_id, created_at DESC);
