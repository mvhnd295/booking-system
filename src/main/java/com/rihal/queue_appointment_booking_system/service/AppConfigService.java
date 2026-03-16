package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.config.AppConfigKeys;
import com.rihal.queue_appointment_booking_system.domain.entity.AppConfig;
import com.rihal.queue_appointment_booking_system.domain.entity.Slot;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.repository.AppConfigRepository;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import com.rihal.queue_appointment_booking_system.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppConfigService {
    private final AppConfigRepository appConfigRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final AuditService auditService;

    // Get retention days
    public int retentionDays() {
        return appConfigRepository.findById(AppConfigKeys.SOFT_DELETE_RETENTION_DAYS)
                .map(c -> Integer.parseInt(c.getValue()))
                .orElse(30);
    }

    // Update retention period
    @Transactional
    public int updateRetentionPeriod(int days, User actor) {
        AppConfig config = appConfigRepository
                .findById(AppConfigKeys.SOFT_DELETE_RETENTION_DAYS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Appconfig key not found. Check migration V13."));

        config.setValue(String.valueOf(days));
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(actor);
        appConfigRepository.save(config);

        log.info("Retention period updated to {} days by {}", days, actor.getUsername());
        return days;
    }

    // Hard delete cleanup (idempotent)
    @Transactional
    public int runCleanUp(User actor) {
        int retentionDays = retentionDays();

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        List<Slot> expiredSlots = slotRepository.findExpiredSoftDeletedSlots(cutoff);

        if (expiredSlots.isEmpty()) {
            log.info("Cleanup job done - no expired soft-deleted slots found.");
            return 0;
        }

        for (Slot slot : expiredSlots) {
            // Nullify slot reference in appointments without deleting appointments
            appointmentRepository.nullifySlotReference(slot.getId());

            // Hard delete slot
            slotRepository.delete(slot);

            // Audit log (old AuditLog entries of soft deleted slots are preserved
            auditService.log(
                    AuditAction.SLOT_HARD_DELETED,
                    actor,
                    EntityType.SLOT,
                    slot.getId(),
                    slot.getBranch(),
                    Map.of(
                            "deletedAt", slot.getDeletedAt().toString(),
                            "retentionDays", String.valueOf(retentionDays)
                    )
            );
        }
        log.info("Cleanup job done - Hard deleted: {} expired slots", expiredSlots.size());
        return expiredSlots.size();
    }
}
