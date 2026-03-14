CREATE TABLE service_types (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    seed_id          VARCHAR(50)  UNIQUE,
    branch_id        UUID         NOT NULL REFERENCES branches(id),
    name             VARCHAR(150) NOT NULL,
    description      TEXT,
    duration_minutes INT          NOT NULL DEFAULT 15,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);