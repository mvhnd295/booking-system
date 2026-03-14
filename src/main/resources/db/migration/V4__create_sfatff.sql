CREATE TABLE staff (
    id        UUID NOT NULL PRIMARY KEY REFERENCES users(id),
    branch_id UUID NOT NULL REFERENCES branches(id),
    phone     VARCHAR(20)
);