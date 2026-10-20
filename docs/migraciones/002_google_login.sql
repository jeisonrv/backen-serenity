BEGIN;

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS google_subject varchar(255);

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_google_subject
    ON usuarios (google_subject)
    WHERE google_subject IS NOT NULL;

COMMIT;
