ALTER TABLE appointments
    DROP CONSTRAINT chk_appointment_status;
ALTER TABLE appointments
    ADD CONSTRAINT chk_appointment_status
        CHECK (status IN ('BOOKED', 'CHECKED_IN', 'COMPLETE', 'CANCELLED', 'NO_SHOW'));