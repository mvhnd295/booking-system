CREATE TABLE audit_logs (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    seed_id            VARCHAR(50)  UNIQUE,
    action             VARCHAR(50)  NOT NULL,
    actor_id           UUID,                    -- nullable: system actions
    actor_role         VARCHAR(50),
    target_entity_type VARCHAR(50)  NOT NULL,
    target_entity_id   VARCHAR(100) NOT NULL,   -- VARCHAR to handle seed string IDs too
    branch_id          UUID         REFERENCES branches(id) ON DELETE SET NULL,
    metadata           JSONB,
    timestamp          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_branch    ON audit_logs(branch_id);
CREATE INDEX idx_audit_logs_actor     ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_action    ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);