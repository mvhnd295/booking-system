package com.rihal.queue_appointment_booking_system.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlotRequest(

        @NotNull(message = "Branch ID is required")
        UUID branchId,

        @NotNull(message = "Service type ID is required")
        UUID serviceTypeId,

        // Optional — slot may not be assigned to a specific staff
        UUID staffId,

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        LocalDateTime startAt,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        LocalDateTime endAt,

        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity,

        boolean active
) {
}
