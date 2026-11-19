--
-- PostgreSQL database dump
--

\restrict t3isBDPQvUvyF8Q6uXKbluGkJnzUbcZ4ZSgslVDgCaDLHJxthqLpxgG1x0bgbFN

-- Dumped from database version 16.16 (Debian 16.16-1.pgdg13+1)
-- Dumped by pg_dump version 16.16 (Debian 16.16-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: app_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_logs (
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    level character varying(16) NOT NULL,
    logger_name character varying(512) NOT NULL,
    thread_name character varying(256) NOT NULL,
    message text NOT NULL,
    stack_trace text,
    application_name character varying(128) NOT NULL,
    hostname character varying(255) NOT NULL,
    trace_id character varying(128),
    user_id character varying(128)
);


--
-- Name: app_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: app_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_logs_id_seq OWNED BY public.app_logs.id;


--
-- Name: consent_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.consent_log (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    consent_type character varying(32) NOT NULL,
    action character varying(16) NOT NULL,
    policy_version character varying(50) NOT NULL,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT consent_log_action_check CHECK (((action)::text = ANY ((ARRAY['GRANTED'::character varying, 'REVOKED'::character varying])::text[]))),
    CONSTRAINT consent_log_consent_type_check CHECK (((consent_type)::text = ANY ((ARRAY['TERMS'::character varying, 'SENSITIVE_DATA'::character varying])::text[])))
);


--
-- Name: consent_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.consent_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: consent_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.consent_log_id_seq OWNED BY public.consent_log.id;


--
-- Name: conversaciones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversaciones (
    id_conversacion bigint NOT NULL,
    fecha_fin timestamp(6) without time zone,
    fecha_inicio timestamp(6) without time zone NOT NULL,
    id_oyente bigint,
    id_usuario bigint NOT NULL
);


--
-- Name: conversaciones_id_conversacion_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversaciones_id_conversacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: conversaciones_id_conversacion_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversaciones_id_conversacion_seq OWNED BY public.conversaciones.id_conversacion;


--
-- Name: entradas_diario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.entradas_diario (
    id_entrada bigint NOT NULL,
    contenido text NOT NULL,
    fecha_actualizacion timestamp(6) without time zone,
    fecha_registro timestamp(6) without time zone NOT NULL,
    id_usuario bigint NOT NULL,
    tipo_prompt character varying(255) NOT NULL,
    titulo character varying(200)
);


--
-- Name: entradas_diario_id_entrada_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.entradas_diario_id_entrada_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: entradas_diario_id_entrada_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.entradas_diario_id_entrada_seq OWNED BY public.entradas_diario.id_entrada;


--
-- Name: estados_animo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.estados_animo (
    id_animo bigint NOT NULL,
    fecha_dia date,
    fecha_registro timestamp(6) without time zone NOT NULL,
    id_usuario bigint NOT NULL,
    valor integer NOT NULL
);


--
-- Name: estados_animo_id_animo_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.estados_animo_id_animo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: estados_animo_id_animo_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.estados_animo_id_animo_seq OWNED BY public.estados_animo.id_animo;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: mensajes_chat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mensajes_chat (
    id bigint NOT NULL,
    contenido text NOT NULL,
    fecha_envio timestamp(6) without time zone NOT NULL,
    id_conversacion bigint NOT NULL,
    remitente character varying(20) NOT NULL
);


--
-- Name: mensajes_chat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mensajes_chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mensajes_chat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.mensajes_chat_id_seq OWNED BY public.mensajes_chat.id;


--
-- Name: oyentes_disponibilidad; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.oyentes_disponibilidad (
    id_oyente bigint NOT NULL,
    disponible boolean NOT NULL,
    fecha_actualizacion timestamp(6) without time zone NOT NULL
);


--
-- Name: recordatorios_enviados; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recordatorios_enviados (
    id bigint NOT NULL,
    usuario_id bigint NOT NULL,
    fecha date NOT NULL,
    fecha_envio timestamp without time zone NOT NULL
);


--
-- Name: recordatorios_enviados_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.recordatorios_enviados_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: recordatorios_enviados_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.recordatorios_enviados_id_seq OWNED BY public.recordatorios_enviados.id;


--
-- Name: reportes_chat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reportes_chat (
    id bigint NOT NULL,
    fecha_reporte timestamp(6) without time zone NOT NULL,
    id_conversacion bigint NOT NULL,
    id_usuario bigint NOT NULL,
    motivo character varying(1000) NOT NULL,
    estado character varying(255) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    fecha_resolucion timestamp(6) without time zone,
    id_moderador bigint,
    id_reportado bigint,
    observacion_moderador character varying(1000),
    CONSTRAINT reportes_chat_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'PROCEDENTE'::character varying, 'IMPROCEDENTE'::character varying])::text[])))
);


--
-- Name: reportes_chat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.reportes_chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: reportes_chat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reportes_chat_id_seq OWNED BY public.reportes_chat.id;


--
-- Name: sesiones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sesiones (
    id bigint NOT NULL,
    duracion_segundos integer NOT NULL,
    fecha_completado timestamp(6) without time zone,
    tipo_ejercicio character varying(255),
    usuario_id bigint,
    xp_ganado integer NOT NULL
);


--
-- Name: sesiones_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sesiones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sesiones_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sesiones_id_seq OWNED BY public.sesiones.id;


--
-- Name: tokens_restablecimiento_password; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tokens_restablecimiento_password (
    id bigint NOT NULL,
    expira_en timestamp(6) with time zone NOT NULL,
    token_hash character varying(64) NOT NULL,
    usado_en timestamp(6) with time zone,
    usuario_id bigint NOT NULL
);


--
-- Name: tokens_restablecimiento_password_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tokens_restablecimiento_password_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tokens_restablecimiento_password_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tokens_restablecimiento_password_id_seq OWNED BY public.tokens_restablecimiento_password.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    mejor_racha integer NOT NULL,
    nivel integer NOT NULL,
    password_hash character varying(255),
    racha integer NOT NULL,
    ultima_actividad date,
    username character varying(255),
    xp integer NOT NULL,
    fecha_restriccion timestamp(6) without time zone,
    motivo_restriccion character varying(1000),
    restringido boolean DEFAULT false NOT NULL,
    restringido_hasta timestamp(6) without time zone,
    restringido_por bigint,
    rol character varying(255) DEFAULT 'USUARIO'::character varying NOT NULL,
    google_subject character varying(255),
    recordatorios_email boolean DEFAULT false NOT NULL,
    terms_accepted_at timestamp with time zone,
    terms_version character varying(50),
    sensitive_consent_at timestamp with time zone,
    sensitive_consent_version character varying(50),
    CONSTRAINT usuarios_rol_check CHECK (((rol)::text = ANY ((ARRAY['USUARIO'::character varying, 'OYENTE'::character varying, 'MODERADOR'::character varying])::text[])))
);


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: app_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_logs ALTER COLUMN id SET DEFAULT nextval('public.app_logs_id_seq'::regclass);


--
-- Name: consent_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consent_log ALTER COLUMN id SET DEFAULT nextval('public.consent_log_id_seq'::regclass);


--
-- Name: conversaciones id_conversacion; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversaciones ALTER COLUMN id_conversacion SET DEFAULT nextval('public.conversaciones_id_conversacion_seq'::regclass);


--
-- Name: entradas_diario id_entrada; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entradas_diario ALTER COLUMN id_entrada SET DEFAULT nextval('public.entradas_diario_id_entrada_seq'::regclass);


--
-- Name: estados_animo id_animo; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estados_animo ALTER COLUMN id_animo SET DEFAULT nextval('public.estados_animo_id_animo_seq'::regclass);


--
-- Name: mensajes_chat id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mensajes_chat ALTER COLUMN id SET DEFAULT nextval('public.mensajes_chat_id_seq'::regclass);


--
-- Name: recordatorios_enviados id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recordatorios_enviados ALTER COLUMN id SET DEFAULT nextval('public.recordatorios_enviados_id_seq'::regclass);


--
-- Name: reportes_chat id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reportes_chat ALTER COLUMN id SET DEFAULT nextval('public.reportes_chat_id_seq'::regclass);


--
-- Name: sesiones id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sesiones ALTER COLUMN id SET DEFAULT nextval('public.sesiones_id_seq'::regclass);


--
-- Name: tokens_restablecimiento_password id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tokens_restablecimiento_password ALTER COLUMN id SET DEFAULT nextval('public.tokens_restablecimiento_password_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Name: app_logs app_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_logs
    ADD CONSTRAINT app_logs_pkey PRIMARY KEY (id);


--
-- Name: consent_log consent_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.consent_log
    ADD CONSTRAINT consent_log_pkey PRIMARY KEY (id);


--
-- Name: conversaciones conversaciones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversaciones
    ADD CONSTRAINT conversaciones_pkey PRIMARY KEY (id_conversacion);


--
-- Name: entradas_diario entradas_diario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entradas_diario
    ADD CONSTRAINT entradas_diario_pkey PRIMARY KEY (id_entrada);


--
-- Name: estados_animo estados_animo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estados_animo
    ADD CONSTRAINT estados_animo_pkey PRIMARY KEY (id_animo);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: tokens_restablecimiento_password idx_token_restablecimiento_hash; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tokens_restablecimiento_password
    ADD CONSTRAINT idx_token_restablecimiento_hash UNIQUE (token_hash);


--
-- Name: mensajes_chat mensajes_chat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mensajes_chat
    ADD CONSTRAINT mensajes_chat_pkey PRIMARY KEY (id);


--
-- Name: oyentes_disponibilidad oyentes_disponibilidad_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.oyentes_disponibilidad
    ADD CONSTRAINT oyentes_disponibilidad_pkey PRIMARY KEY (id_oyente);


--
-- Name: recordatorios_enviados recordatorios_enviados_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recordatorios_enviados
    ADD CONSTRAINT recordatorios_enviados_pkey PRIMARY KEY (id);


--
-- Name: reportes_chat reportes_chat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reportes_chat
    ADD CONSTRAINT reportes_chat_pkey PRIMARY KEY (id);


--
-- Name: sesiones sesiones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sesiones
    ADD CONSTRAINT sesiones_pkey PRIMARY KEY (id);


--
-- Name: tokens_restablecimiento_password tokens_restablecimiento_password_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tokens_restablecimiento_password
    ADD CONSTRAINT tokens_restablecimiento_password_pkey PRIMARY KEY (id);


--
-- Name: estados_animo uk_animo_usuario_dia; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.estados_animo
    ADD CONSTRAINT uk_animo_usuario_dia UNIQUE (id_usuario, fecha_dia);


--
-- Name: usuarios uk_kfsp0s1tflm1cwlj8idhqsad0; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uk_kfsp0s1tflm1cwlj8idhqsad0 UNIQUE (email);


--
-- Name: usuarios uk_kgjdj99qywrad378cougc8qgt; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT uk_kgjdj99qywrad378cougc8qgt UNIQUE (google_subject);


--
-- Name: recordatorios_enviados uk_recordatorio_usuario_fecha; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recordatorios_enviados
    ADD CONSTRAINT uk_recordatorio_usuario_fecha UNIQUE (usuario_id, fecha);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_app_logs_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_logs_created_at ON public.app_logs USING btree (created_at);


--
-- Name: idx_app_logs_level; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_logs_level ON public.app_logs USING btree (level);


--
-- Name: idx_app_logs_trace_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_logs_trace_id ON public.app_logs USING btree (trace_id);


--
-- Name: idx_consent_log_user_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_consent_log_user_created ON public.consent_log USING btree (user_id, created_at DESC);


--
-- Name: idx_recordatorios_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_recordatorios_fecha ON public.recordatorios_enviados USING btree (fecha);


--
-- PostgreSQL database dump complete
--

\unrestrict t3isBDPQvUvyF8Q6uXKbluGkJnzUbcZ4ZSgslVDgCaDLHJxthqLpxgG1x0bgbFN

