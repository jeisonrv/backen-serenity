BEGIN;

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS rol varchar(20) NOT NULL DEFAULT 'USUARIO';
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS restringido boolean NOT NULL DEFAULT false;
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS motivo_restriccion varchar(1000);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS restringido_hasta timestamp(6);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS fecha_restriccion timestamp(6);
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS restringido_por bigint;

ALTER TABLE conversaciones ALTER COLUMN id_oyente DROP NOT NULL;
-- El código anterior asignaba siempre el oyente 1L; se limpian esas asignaciones para evitar exponerle charlas ajenas.
UPDATE conversaciones SET id_oyente = NULL WHERE id_oyente = 1;
UPDATE conversaciones SET fecha_fin = now() WHERE id_oyente IS NULL AND fecha_fin IS NULL;

ALTER TABLE reportes_chat ADD COLUMN IF NOT EXISTS id_reportado bigint;
ALTER TABLE reportes_chat ADD COLUMN IF NOT EXISTS estado varchar(20) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE reportes_chat ADD COLUMN IF NOT EXISTS id_moderador bigint;
ALTER TABLE reportes_chat ADD COLUMN IF NOT EXISTS observacion_moderador varchar(1000);
ALTER TABLE reportes_chat ADD COLUMN IF NOT EXISTS fecha_resolucion timestamp(6);
CREATE INDEX IF NOT EXISTS idx_reportes_chat_estado ON reportes_chat (estado);
CREATE INDEX IF NOT EXISTS idx_reportes_chat_reportado_estado ON reportes_chat (id_reportado, estado);

CREATE TABLE IF NOT EXISTS oyentes_disponibilidad (
    id_oyente bigint PRIMARY KEY,
    disponible boolean NOT NULL,
    fecha_actualizacion timestamp(6) NOT NULL
);

-- Asignación manual de roles (no existe endpoint para hacerlo):
-- UPDATE usuarios SET rol = 'OYENTE' WHERE email = 'oyente@example.com';
-- UPDATE usuarios SET rol = 'MODERADOR' WHERE email = 'moderador@example.com';
COMMIT;
