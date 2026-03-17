package com.rihal.queue_appointment_booking_system.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record QueuePositionResponse(
        UUID appointmentId,
        UUID slotId,
        LocalDateTime slotStartAt,
        //nullable fields if slot was deleted
        Integer queuePosition,
        Integer totalInQueue,
        Integer estimatedWaitTime
) {
}
