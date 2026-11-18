
-- =========================================================
-- V4: Creación de las tablas principales de Serenity
-- Compatible con la estructura existente de PostgreSQL 16.
-- =========================================================

-- 1. USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
                                        id BIGINT NOT NULL,
                                        email VARCHAR(255) NOT NULL,
    mejor_racha INTEGER NOT NULL,
    nivel INTEGER NOT NULL,
    password_hash VARCHAR(255),
    racha INTEGER NOT NULL,
    ultima_actividad DATE,
    username VARCHAR(255),
    xp INTEGER NOT NULL,
    fecha_restriccion TIMESTAMP(6),
    motivo_restriccion VARCHAR(1000),
    restringido BOOLEAN NOT NULL DEFAULT FALSE,
    restringido_hasta TIMESTAMP(6),
    restringido_por BIGINT,
    rol VARCHAR(255) NOT NULL DEFAULT 'USUARIO',
    google_subject VARCHAR(255),
    recordatorios_email BOOLEAN NOT NULL DEFAULT FALSE,
    terms_accepted_at TIMESTAMPTZ,
    terms_version VARCHAR(50),
    sensitive_consent_at TIMESTAMPTZ,
    sensitive_consent_version VARCHAR(50),
    CONSTRAINT usuarios_pkey PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_google_subject UNIQUE (google_subject),
    CONSTRAINT usuarios_rol_check
    CHECK (rol IN ('USUARIO', 'OYENTE', 'MODERADOR'))
    );

-- 2. CONVERSACIONES
CREATE TABLE IF NOT EXISTS conversaciones (
                                              id_conversacion BIGINT NOT NULL,
                                              fecha_fin TIMESTAMP(6),
    fecha_inicio TIMESTAMP(6) NOT NULL,
    id_oyente BIGINT,
    id_usuario BIGINT NOT NULL,
    CONSTRAINT conversaciones_pkey PRIMARY KEY (id_conversacion)
    );

-- 3. MENSAJES DEL CHAT
CREATE TABLE IF NOT EXISTS mensajes_chat (
                                             id BIGINT NOT NULL,
                                             contenido TEXT NOT NULL,
                                             fecha_envio TIMESTAMP(6) NOT NULL,
    id_conversacion BIGINT NOT NULL,
    remitente VARCHAR(20) NOT NULL,
    CONSTRAINT mensajes_chat_pkey PRIMARY KEY (id)
    );

-- 4. REPORTES DEL CHAT
CREATE TABLE IF NOT EXISTS reportes_chat (
                                             id BIGINT NOT NULL,
                                             fecha_reporte TIMESTAMP(6) NOT NULL,
    id_conversacion BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    motivo VARCHAR(1000) NOT NULL,
    estado VARCHAR(255) NOT NULL DEFAULT 'PENDIENTE',
    fecha_resolucion TIMESTAMP(6),
    id_moderador BIGINT,
    id_reportado BIGINT,
    observacion_moderador VARCHAR(1000),
    CONSTRAINT reportes_chat_pkey PRIMARY KEY (id),
    CONSTRAINT reportes_chat_estado_check
    CHECK (estado IN ('PENDIENTE', 'PROCEDENTE', 'IMPROCEDENTE'))
    );

-- 5. ESTADOS DE ÁNIMO
CREATE TABLE IF NOT EXISTS estados_animo (
                                             id_animo BIGINT NOT NULL,
                                             fecha_dia DATE,
                                             fecha_registro TIMESTAMP(6) NOT NULL,
    id_usuario BIGINT NOT NULL,
    valor INTEGER NOT NULL,
    CONSTRAINT estados_animo_pkey PRIMARY KEY (id_animo),
    CONSTRAINT uk_animo_usuario_dia UNIQUE (id_usuario, fecha_dia)
    );

-- 6. ENTRADAS DEL DIARIO
CREATE TABLE IF NOT EXISTS entradas_diario (
                                               id_entrada BIGINT NOT NULL,
                                               contenido TEXT NOT NULL,
                                               fecha_actualizacion TIMESTAMP(6),
    fecha_registro TIMESTAMP(6) NOT NULL,
    id_usuario BIGINT NOT NULL,
    tipo_prompt VARCHAR(255) NOT NULL,
    titulo VARCHAR(200),
    CONSTRAINT entradas_diario_pkey PRIMARY KEY (id_entrada)
    );

-- 7. SESIONES
CREATE TABLE IF NOT EXISTS sesiones (
                                        id BIGINT NOT NULL,
                                        duracion_segundos INTEGER NOT NULL,
                                        fecha_completado TIMESTAMP(6),
    tipo_ejercicio VARCHAR(255),
    usuario_id BIGINT,
    xp_ganado INTEGER NOT NULL,
    CONSTRAINT sesiones_pkey PRIMARY KEY (id)
    );

-- 8. RECORDATORIOS ENVIADOS
CREATE TABLE IF NOT EXISTS recordatorios_enviados (
                                                      id BIGINT NOT NULL,
                                                      usuario_id BIGINT NOT NULL,
                                                      fecha DATE NOT NULL,
                                                      fecha_envio TIMESTAMP NOT NULL,
                                                      CONSTRAINT recordatorios_enviados_pkey PRIMARY KEY (id),
    CONSTRAINT uk_recordatorio_usuario_fecha UNIQUE (usuario_id, fecha)
    );

-- 9. DISPONIBILIDAD DE OYENTES
CREATE TABLE IF NOT EXISTS oyentes_disponibilidad (
                                                      id_oyente BIGINT NOT NULL,
                                                      disponible BOOLEAN NOT NULL,
                                                      fecha_actualizacion TIMESTAMP(6) NOT NULL,
    CONSTRAINT oyentes_disponibilidad_pkey PRIMARY KEY (id_oyente)
    );

-- 10. TOKENS DE RESTABLECIMIENTO DE CONTRASEÑA
CREATE TABLE IF NOT EXISTS tokens_restablecimiento_password (
                                                                id BIGINT NOT NULL,
                                                                expira_en TIMESTAMPTZ NOT NULL,
                                                                token_hash VARCHAR(64) NOT NULL,
    usado_en TIMESTAMPTZ,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT tokens_restablecimiento_password_pkey PRIMARY KEY (id),
    CONSTRAINT idx_token_restablecimiento_hash UNIQUE (token_hash)
    );

-- =========================================================
-- SECUENCIAS PARA LOS IDENTIFICADORES
-- =========================================================

CREATE SEQUENCE IF NOT EXISTS usuarios_id_seq;
CREATE SEQUENCE IF NOT EXISTS conversaciones_id_conversacion_seq;
CREATE SEQUENCE IF NOT EXISTS mensajes_chat_id_seq;
CREATE SEQUENCE IF NOT EXISTS reportes_chat_id_seq;
CREATE SEQUENCE IF NOT EXISTS estados_animo_id_animo_seq;
CREATE SEQUENCE IF NOT EXISTS entradas_diario_id_entrada_seq;
CREATE SEQUENCE IF NOT EXISTS sesiones_id_seq;
CREATE SEQUENCE IF NOT EXISTS recordatorios_enviados_id_seq;
CREATE SEQUENCE IF NOT EXISTS tokens_restablecimiento_password_id_seq;

ALTER SEQUENCE usuarios_id_seq OWNED BY usuarios.id;
ALTER SEQUENCE conversaciones_id_conversacion_seq
    OWNED BY conversaciones.id_conversacion;
ALTER SEQUENCE mensajes_chat_id_seq OWNED BY mensajes_chat.id;
ALTER SEQUENCE reportes_chat_id_seq OWNED BY reportes_chat.id;
ALTER SEQUENCE estados_animo_id_animo_seq OWNED BY estados_animo.id_animo;
ALTER SEQUENCE entradas_diario_id_entrada_seq OWNED BY entradas_diario.id_entrada;
ALTER SEQUENCE sesiones_id_seq OWNED BY sesiones.id;
ALTER SEQUENCE recordatorios_enviados_id_seq
    OWNED BY recordatorios_enviados.id;
ALTER SEQUENCE tokens_restablecimiento_password_id_seq
    OWNED BY tokens_restablecimiento_password.id;

-- Asignar las secuencias como valores predeterminados.
ALTER TABLE usuarios
    ALTER COLUMN id SET DEFAULT nextval('usuarios_id_seq');

ALTER TABLE conversaciones
    ALTER COLUMN id_conversacion
        SET DEFAULT nextval('conversaciones_id_conversacion_seq');

ALTER TABLE mensajes_chat
    ALTER COLUMN id SET DEFAULT nextval('mensajes_chat_id_seq');

ALTER TABLE reportes_chat
    ALTER COLUMN id SET DEFAULT nextval('reportes_chat_id_seq');

ALTER TABLE estados_animo
    ALTER COLUMN id_animo SET DEFAULT nextval('estados_animo_id_animo_seq');

ALTER TABLE entradas_diario
    ALTER COLUMN id_entrada SET DEFAULT nextval('entradas_diario_id_entrada_seq');

ALTER TABLE sesiones
    ALTER COLUMN id SET DEFAULT nextval('sesiones_id_seq');

ALTER TABLE recordatorios_enviados
    ALTER COLUMN id SET DEFAULT nextval('recordatorios_enviados_id_seq');

ALTER TABLE tokens_restablecimiento_password
    ALTER COLUMN id SET DEFAULT nextval('tokens_restablecimiento_password_id_seq');

-- Índice para consultar los recordatorios por fecha.
CREATE INDEX IF NOT EXISTS idx_recordatorios_fecha
    ON recordatorios_enviados (fecha);
