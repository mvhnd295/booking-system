package com.rihal.queue_appointment_booking_system.audit;

import com.rihal.queue_appointment_booking_system.domain.entity.AuditLog;
import com.rihal.queue_appointment_booking_system.domain.entity.Branch;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    /**
     * Log a sensitive action performed by a user.
     *
     * @param action           what happened
     * @param actor            who did it
     * @param targetEntityType what kind of entity was affected
     * @param targetEntityId   the affected entity's UUID
     * @param branch           the branch context (nullable for system-wide actions)
     * @param metadata         optional extra info (e.g. old/new slot IDs for reschedule)
     */
    public void log(
            AuditAction action,
            User actor,
            EntityType targetEntityType,
            UUID targetEntityId,
            Branch branch,
            Map<String, Object> metadata
    ) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setActorId(actor.getId());
        entry.setActorRole(actor.getRole().getName());
        entry.setTargetEntityType(targetEntityType);
        entry.setTargetEntityId(targetEntityId.toString());
        entry.setBranch(branch);
        entry.setMetadata(metadata);
        entry.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(entry);
    }
    // convenience overload without metadata
    public void log(
            AuditAction action,
            User actor,
            EntityType targetEntityType,
            UUID targetEntityId,
            Branch branch
    ) {
        log(action, actor, targetEntityType, targetEntityId, branch, null);
    }

    // System/scheduler-triggered log (no actor)
    public void logSystem(
            AuditAction action,
            EntityType targetEntityType,
            UUID targetEntityId,
            Branch branch,
            Map<String, Object> metadata
    ) {
        AuditLog entry = new AuditLog();
        entry.setAction(action);
        entry.setActorId(null);   // no user — system action
        entry.setActorRole(null); // no role
        entry.setTargetEntityType(targetEntityType);
        entry.setTargetEntityId(targetEntityId.toString());
        entry.setBranch(branch);
        entry.setMetadata(metadata);
        entry.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(entry);
    }
}
