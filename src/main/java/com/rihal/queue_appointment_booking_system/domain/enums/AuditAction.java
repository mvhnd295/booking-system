package com.rihal.queue_appointment_booking_system.domain.enums;

public enum AuditAction {
    // Appointments
    APPOINTMENT_BOOKED,
    APPOINTMENT_RESCHEDULED,
    APPOINTMENT_CANCELLED,
    APPOINTMENT_STATUS_UPDATED,

    // Slot
    SLOT_CREATED,
    SLOT_UPDATED,
    SLOT_SOFT_DELETED,
    SLOT_HARD_DELETED,

    // Staff
    STAFF_ASSIGNED_TO_SERVICE,
    STAFF_REMOVED_FROM_SERVICE,

    // System
    SEED_IMPORT
}
