CREATE TABLE attachments (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    file_path     VARCHAR(500) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    mime_type     VARCHAR(100) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    uploaded_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);