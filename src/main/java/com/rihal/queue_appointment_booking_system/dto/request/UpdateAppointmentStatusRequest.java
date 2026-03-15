package com.rihal.queue_appointment_booking_system.dto.request;

import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull(message = "Status is required.")
        AppointmentStatus status
) {
}
