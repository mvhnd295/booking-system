package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.audit.AuditService;
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
import java.util.UUID;
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

    // ── Rate limit config ─────────────────────────────────────────────────────

    // Get max bookings per day
    public int getMaxBookingsPerDay() {
        return appConfigRepository.findById(AppConfigKeys.MAX_BOOKING_PER_DAY)
                .map(c -> Integer.parseInt(c.getValue()))
                .orElse(3);
    }

    // Get max reschedules per day
    public int getMaxReschedulesPerDay() {
        return appConfigRepository.findById(AppConfigKeys.MAX_RESCHEDULE_PER_DAY)
                .map(c -> Integer.parseInt(c.getValue()))
                .orElse(2);
    }

    // Update max bookings per day
    @Transactional
    public int updateMaxBookingsPerDay(int max, User actor) {
        AppConfig config = appConfigRepository
                .findById(AppConfigKeys.MAX_BOOKING_PER_DAY)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Appconfig key not found. Check migration V19."));

        config.setValue(String.valueOf(max));
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(actor);
        appConfigRepository.save(config);

        log.info("Max bookings per day updated to {} by {}", max, actor.getUsername());
        return max;
    }

    // Update max reschedules per day
    @Transactional
    public int updateMaxReschedulesPerDay(int max, User actor) {
        AppConfig config = appConfigRepository
                .findById(AppConfigKeys.MAX_RESCHEDULE_PER_DAY)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Appconfig key not found. Check migration V19."));

        config.setValue(String.valueOf(max));
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(actor);
        appConfigRepository.save(config);

        log.info("Max reschedules per day updated to {} by {}", max, actor.getUsername());
        return max;
    }


    /**
     * Hard delete cleanup (idempotent)
     * @param actor -> user triggering the cleanup (null if triggered by scheduler)
     * @return number of expired soft-deleted slots (0 if none found)
     */
    @Transactional
    public int runCleanUp(User actor) {
        int retentionDays = retentionDays();

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        List<Slot> expiredSlots = slotRepository.findExpiredSoftDeletedSlots(cutoff);

        if (expiredSlots.isEmpty()) {
            log.info("Cleanup job done - no expired soft-deleted slots found.");
            return 0;
        }

        // Nullify all slot references in one UPDATE instead of one per slot.
        // The original loop called nullifySlotReference(id) N times, producing N round-trips.
        List<UUID> expiredSlotIds = expiredSlots.stream().map(Slot::getId).toList();
        appointmentRepository.nullifySlotReferences(expiredSlotIds);

        // deleteAll issues individual DELETEs but within a single flush, which is standard JPA batch behaviour
        slotRepository.deleteAll(expiredSlots);

        // Audit log per slot — intentionally kept per-item (each hard-delete is a distinct audited event)
        for (Slot slot : expiredSlots) {
            if (actor != null) {
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
            } else {
                auditService.logSystem(
                        AuditAction.SLOT_HARD_DELETED,
                        EntityType.SLOT,
                        slot.getId(),
                        slot.getBranch(),
                        Map.of(
                                "deletedAt", slot.getDeletedAt().toString(),
                                "retentionDays", String.valueOf(retentionDays),
                                "SCHEDULER", true
                        )
                );
            }
        }
        log.info("Cleanup job done - Hard deleted: {} expired slot(s)", expiredSlots.size());
        return expiredSlots.size();
    }
}
