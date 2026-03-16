package com.rihal.queue_appointment_booking_system.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RetentionDayRequest(
        @NotNull(message = "Day is required.")
        @Min(value = 1, message = "Retention period must be at least 1 day.")
        Integer days
) {}
