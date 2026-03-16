package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.AuditLog;

public class AuditLogMapper {
    private AuditLogMapper() {}

    public static AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getActorId(),
                log.getActorRole(),
                log.getTargetEntityType(),
                log.getTargetEntityId(),
                log.getBranch() != null ? log.getBranch().getId() : null,
                log.getBranch() != null ? log.getBranch().getName() : null,
                log.getMetadata(),
                log.getTimestamp()
        );
    }
}
