CREATE TABLE appointments (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    seed_id         VARCHAR(50) UNIQUE,
    customer_id     UUID        NOT NULL REFERENCES customers(id),
    slot_id         UUID        REFERENCES slots(id) ON DELETE SET NULL,  -- null on hard delete
    branch_id       UUID        NOT NULL REFERENCES branches(id),         -- denormalized
    service_type_id UUID        NOT NULL REFERENCES service_types(id),    -- denormalized
    staff_id        UUID        REFERENCES staff(id),                     -- denormalized, nullable
    attachment_id   UUID        REFERENCES attachments(id) ON DELETE SET NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    -- PENDING, CHECKED_IN, COMPLETED, CANCELLED, NO_SHOW
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_appointment_status
        CHECK (status IN ('PENDING','CHECKED_IN','COMPLETED','CANCELLED','NO_SHOW'))
);

CREATE INDEX idx_appointments_customer  ON appointments(customer_id);
CREATE INDEX idx_appointments_slot      ON appointments(slot_id);
CREATE INDEX idx_appointments_branch    ON appointments(branch_id);
CREATE INDEX idx_appointments_status    ON appointments(status);