CREATE TABLE slots (
    id              UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    seed_id         VARCHAR(50) UNIQUE,
    branch_id       UUID      NOT NULL REFERENCES branches(id),
    service_type_id UUID      NOT NULL REFERENCES service_types(id),
    staff_id        UUID      REFERENCES staff(id),       -- nullable
    start_at        TIMESTAMP NOT NULL,
    end_at          TIMESTAMP NOT NULL,
    capacity        INT       NOT NULL DEFAULT 1,
    booked          INT       NOT NULL DEFAULT 0,
    is_active       BOOLEAN   NOT NULL DEFAULT TRUE,

    -- soft delete
    deleted         BOOLEAN   NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,

    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_slot_times    CHECK (end_at > start_at),
    CONSTRAINT chk_booked_capacity CHECK (booked <= capacity)
);

CREATE INDEX idx_slots_branch        ON slots(branch_id);
CREATE INDEX idx_slots_service_type  ON slots(service_type_id);
CREATE INDEX idx_slots_staff         ON slots(staff_id);
CREATE INDEX idx_slots_start_at      ON slots(start_at);
CREATE INDEX idx_slots_deleted       ON slots(deleted);