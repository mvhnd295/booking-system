CREATE TABLE app_config (
    key        VARCHAR(100) PRIMARY KEY,
    value      VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_by UUID         REFERENCES users(id)
);

-- Default retention period: 30 days
INSERT INTO app_config (key, value) VALUES ('SOFT_DELETE_RETENTION_DAYS', '30');
