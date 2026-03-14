package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.ServiceType;

import java.util.UUID;

public record ServiceTypeResponse(
        UUID id,
        String name,
        String description,
        int durationMinutes
) {
    public static ServiceTypeResponse from(ServiceType serviceType) {
        return new ServiceTypeResponse(
                serviceType.getId(),
                serviceType.getName(),
                serviceType.getDescription(),
                serviceType.getDurationMinutes()
        );
    }
}
