package com.rihal.queue_appointment_booking_system.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateSlotRequest(
        // all fields are nullable
        // null = dont change
        UUID serviceTypeId,
        UUID staffId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer capacity,
        Boolean active
) {
}
