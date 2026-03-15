package com.rihal.queue_appointment_booking_system.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlotManagementResponse(
        UUID id,
        UUID branchId,
        String branchName,
        UUID serviceTypeId,
        String serviceTypeName,
        UUID staffId,
        String staffName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int capacity,
        int booked,
        int available,
        boolean active,
        boolean deleted,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
