package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.entity.Slot;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentMapper;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentResponse;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import com.rihal.queue_appointment_booking_system.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentManagementService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final BranchSecurityService branchSecurityService;
    private final AuditService auditService;

    // List of statuses that staff/branch-managers/admin can update (staff and managers are branch-scoped
    Set<AppointmentStatus> UPDATEABLE_STATUSES =
            Set.of(
                    AppointmentStatus.CHECKED_IN,
                    AppointmentStatus.NO_SHOW,
                    AppointmentStatus.COMPLETED
            );

    // List appointments
    /**
     *  branchId = null -> Admin -> return all appointments
     *  branchId != null -> Manager -> return all appointments of that branch
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> listAppointments(User actor, UUID branchId) {

        UUID allowedBranch = branchSecurityService.resolveAllowedBranchId(actor, branchId);

        List<Appointment> appointments = (allowedBranch == null)
                ? appointmentRepository.findAllByOrderByCreatedAtDesc()
                : appointmentRepository.findByBranchIdOrderByCreatedAtDesc(allowedBranch);

        return appointments.stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    // Get a single appointment
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointment(User actor, UUID appointmentId) {

        Appointment appointment = resolveAppointmentWithBranchCheck(actor, appointmentId);
        return AppointmentMapper.toResponse(appointment);
    }

    // Update appointment status (Admin & Manager)
    @Transactional
    public AppointmentResponse updateStatus(User actor, UUID appointmentId, AppointmentStatus newStatus) {
        if (!UPDATEABLE_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Allowed statuses: CHECKED-IN, NO-SHOW, COMPLETED.");
        }
        Appointment appointment = resolveAppointmentWithBranchCheck(actor, appointmentId);
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(newStatus);
        appointmentRepository.saveAndFlush(appointment);

        auditService.log(
                AuditAction.APPOINTMENT_STATUS_UPDATED,
                actor,
                EntityType.APPOINTMENT,
                appointment.getId(),
                appointment.getBranch(),
                Map.of("oldStatus", oldStatus.name(), "newStatus", newStatus.name())
        );

        return AppointmentMapper.toResponse(appointment);
    }

    // Cancel appointment (Admin & Manager)
    public AppointmentResponse cancel(User actor, UUID appointmentId) {
        Appointment appointment = resolveAppointmentWithBranchCheck(actor, appointmentId);

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException(
                    "Only BOOKED appointments can be cancelled. Current status: " + appointment.getStatus()
            );
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(appointment);

        // Release the Slot that was occupied by this appointment
        Slot slot = appointment.getSlot();
        if (slot != null && slot.getBooked() > 0) {
            slot.setBooked(slot.getBooked() - 1);
            slotRepository.save(slot);
        }

        // Audit this sensitive action
        auditService.log(
                AuditAction.APPOINTMENT_CANCELLED,
                actor,
                EntityType.APPOINTMENT,
                appointment.getId(),
                appointment.getBranch()
        );

        return AppointmentMapper.toResponse(appointment);
    }


    // Private helper
    private Appointment resolveAppointmentWithBranchCheck(User actor, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Appointment not found: "+ appointmentId));
        // Throw 403 forbidden if manager tries to access appointment from another branch
        branchSecurityService.assertBranchAccess(actor, appointment.getBranch().getId());
        return appointment;
    }
}
