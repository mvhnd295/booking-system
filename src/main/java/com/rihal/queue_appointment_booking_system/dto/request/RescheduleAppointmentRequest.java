package com.rihal.queue_appointment_booking_system.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RescheduleAppointmentRequest(
        @NotNull(message = "Slot ID is required!")
        UUID newSlotId
) {}
