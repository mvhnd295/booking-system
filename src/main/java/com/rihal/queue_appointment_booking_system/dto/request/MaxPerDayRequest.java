package com.rihal.queue_appointment_booking_system.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MaxPerDayRequest(
        @NotNull(message = "Max value is required.")
        @Min(value = 1, message = "Max value must be at least 1.")
        Integer max
) {}
