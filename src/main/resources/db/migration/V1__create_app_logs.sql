CREATE TABLE IF NOT EXISTS app_logs (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    level VARCHAR(16) NOT NULL,
    logger_name VARCHAR(512) NOT NULL,
    thread_name VARCHAR(256) NOT NULL,
    message TEXT NOT NULL,
    stack_trace TEXT,
    application_name VARCHAR(128) NOT NULL,
    hostname VARCHAR(255) NOT NULL,
    trace_id VARCHAR(128),
    user_id VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_app_logs_created_at ON app_logs (created_at);
CREATE INDEX IF NOT EXISTS idx_app_logs_level ON app_logs (level);
CREATE INDEX IF NOT EXISTS idx_app_logs_trace_id ON app_logs (trace_id);
