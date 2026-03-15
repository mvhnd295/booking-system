package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        AppointmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        // Slot info (will be null if slot was hard-deleted)
        UUID slotId,
        LocalDateTime slotStartAt,
        LocalDateTime slotEndAt,

        // Denormalized context - Data snapshot at booking time
        // Preserving historical accuracy even if records in their respective tables change
        UUID branchId,
        String branchName,
        UUID serviceTypeId,
        String serviceTypeName,
        UUID staffId,
        String staffName,

        // Attachment (will be null if no attachment was uploaded since optional for appointments)
        UUID attachmentId,
        String attachmentUrl,
        String originalName,
        String mimeType,
        Long sizeBytes
) {}
