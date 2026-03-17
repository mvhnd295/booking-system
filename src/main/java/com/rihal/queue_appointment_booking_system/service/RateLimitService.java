package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.exception.RateLimitExceededException;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import com.rihal.queue_appointment_booking_system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final AppointmentRepository appointmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final AppConfigService appConfigService;

    /**
     * Checks whether the customer has exceeded their daily booking limit.
     * @param customerId the customer's UUID
     * @throws RateLimitExceededException if the limit is reached
     */
    @Transactional(readOnly = true)
    public void checkBookingLimit(UUID customerId) {
        int max = appConfigService.getMaxBookingsPerDay();
        long todayCount = appointmentRepository.countTodayBookingsByCustomer(customerId);

        if (todayCount >= max) {
            throw new RateLimitExceededException(
                    "Daily booking limit reached (" + max + " per day). Please try again tomorrow."
            );
        }
    }

    /**
     * Checks whether the actor has exceeded their daily reschedule limit.
     * @param actorId the actor's (user) UUID
     * @throws RateLimitExceededException if the limit is reached
     */
    @Transactional(readOnly = true)
    public void checkRescheduleLimit(UUID actorId) {
        int max = appConfigService.getMaxReschedulesPerDay();
        long todayCount = auditLogRepository.countTodayByActorAndAction(
                actorId, AuditAction.APPOINTMENT_RESCHEDULED
        );

        if (todayCount >= max) {
            throw new RateLimitExceededException(
                    "Daily reschedule limit reached (" + max + " per day). Please try again tomorrow."
            );
        }
    }
}
