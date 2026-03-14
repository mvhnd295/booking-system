CREATE TABLE branches (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    seed_id    VARCHAR(50)  UNIQUE,
    name       VARCHAR(150) NOT NULL,
    city       VARCHAR(100),
    address    VARCHAR(255),
    timezone   VARCHAR(50)  NOT NULL DEFAULT 'Asia/Muscat',
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);