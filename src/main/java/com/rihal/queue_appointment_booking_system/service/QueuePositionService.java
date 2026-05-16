package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.dto.response.QueuePositionResponse;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueuePositionService {
    private final AppointmentRepository appRepo;

    //Statuses that count as in the queue (active)
    private static final List<AppointmentStatus> IN_QUEUE_STATUSES =
            List.of(
                    AppointmentStatus.BOOKED,
                    AppointmentStatus.CHECKED_IN
            );
    // get queue position for customer
    @Transactional(readOnly = true)
    public QueuePositionResponse getQueuePosition(User actor, UUID appointmentId) {
        Appointment appointment = appRepo.findByIdWithAssociations(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Appointment not found."
                ));
        if (!appointment.getCustomer().getId().equals(actor.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have access to this appointment."
            );
        }
        // nullify queue info if slot got hard deleted
        if (appointment.getSlot() == null) {
            return new QueuePositionResponse(
                    appointment.getId(),
                    null, null, null, null, null);
        }
        // Check appointment status
        if (!IN_QUEUE_STATUSES.contains(appointment.getStatus())) {
            return new QueuePositionResponse(
                    appointment.getId(),
                    appointment.getSlot().getId(),
                    appointment.getSlot().getStartAt(),
                    null, null, null);
        }
        UUID slotId = appointment.getSlot().getId();

        // Count how many appointments ahead of this one
        long countAhead = appRepo.countAheadInQueue(slotId, IN_QUEUE_STATUSES, appointment.getCreatedAt());
        int queuePosition = (int) countAhead + 1;
        long totalInQueue = appRepo.countTotalInQueue(slotId, IN_QUEUE_STATUSES);
        // Calc estimated wait time
        // Number of people ahead * Service type duration
        int durationMinutes = appointment.getServiceType().getDurationMinutes();
        int estimatedWaitTime = (queuePosition - 1) * durationMinutes;

        return new QueuePositionResponse(
                appointmentId,
                slotId,
                appointment.getSlot().getStartAt(),
                queuePosition,
                (int) totalInQueue,
                estimatedWaitTime
        );
    }
}
