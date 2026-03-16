package com.rihal.queue_appointment_booking_system.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AssignServiceRequest(
        @NotNull(message = "At least 1 service type ID is required.")
        List<UUID> serviceTypeIds
) {}
