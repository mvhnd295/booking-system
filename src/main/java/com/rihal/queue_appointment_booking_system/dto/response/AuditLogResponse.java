package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.domain.enums.RoleName;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        AuditAction action,
        UUID actorId,
        RoleName actorRole,
        EntityType targetEntityType,
        String targetEntityId,
        UUID branchId,
        String branchName,
        Map<String, Object> metadata,
        LocalDateTime timestamp
) {}
