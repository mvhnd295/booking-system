CREATE TABLE customers (
    id              UUID NOT NULL PRIMARY KEY REFERENCES users(id),
    phone           VARCHAR(20),
    id_image_path   VARCHAR(500),   -- secured file ref, populated on real registration
    id_image_size   BIGINT,
    id_image_type   VARCHAR(50)
);