-- Enable pg_trgm extension for trigram-based GIN indexes (fast LIKE '%term%' queries)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Users: searchable by full_name, email, username
CREATE INDEX idx_users_full_name_trgm ON users USING GIN (full_name gin_trgm_ops);
CREATE INDEX idx_users_email_trgm     ON users USING GIN (email gin_trgm_ops);
CREATE INDEX idx_users_username_trgm  ON users USING GIN (username gin_trgm_ops);

-- Branches: searchable by name
CREATE INDEX idx_branches_name_trgm ON branches USING GIN (name gin_trgm_ops);

-- Service Types: searchable by name
CREATE INDEX idx_service_types_name_trgm ON service_types USING GIN (name gin_trgm_ops);

-- Audit Logs: searchable by action, actor_role
CREATE INDEX idx_audit_logs_action_trgm     ON audit_logs USING GIN (action gin_trgm_ops);
CREATE INDEX idx_audit_logs_actor_role_trgm ON audit_logs USING GIN (actor_role gin_trgm_ops);
