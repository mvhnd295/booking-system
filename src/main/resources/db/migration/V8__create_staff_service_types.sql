CREATE TABLE staff_service_types (
    staff_id        UUID NOT NULL REFERENCES staff(id)        ON DELETE CASCADE,
    service_type_id UUID NOT NULL REFERENCES service_types(id) ON DELETE CASCADE,
    PRIMARY KEY (staff_id, service_type_id)
);