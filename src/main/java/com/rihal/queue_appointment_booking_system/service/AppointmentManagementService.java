package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.audit.AuditService;
import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.entity.Slot;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentMapper;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffAppointmentResponse;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import com.rihal.queue_appointment_booking_system.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentManagementService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final BranchSecurityService branchSecurityService;
    private final AuditService auditService;

    // List of statuses that staff/branch-managers/admin can update (staff and managers are branch-scoped
    Set<AppointmentStatus> UPDATABLE_STATUSES =
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
    @Cacheable(value = "appointments", key = "#actor.id + '_' + #branchId + '_' + #term + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResponse<StaffAppointmentResponse> listAppointments(User actor, UUID branchId, String term, Pageable pageable) {

        UUID allowedBranch = branchSecurityService.resolveAllowedBranchId(actor, branchId);

        // Two-query pattern: 1) fetch IDs with SQL-level pagination
        Page<UUID> idPage = (allowedBranch == null)
                ? appointmentRepository.findAllIds(term, pageable)
                : appointmentRepository.findIdsByBranch(term, allowedBranch, pageable);

        if (idPage.isEmpty()) {
            return PagedResponse.from(idPage, List.of());
        }

        // 2) Fetch full entities with all associations eagerly loaded
        List<Appointment> appointments = appointmentRepository.findAllWithAssociationsByIds(idPage.getContent());
        Map<UUID, Appointment> byId = appointments.stream()
                .collect(Collectors.toMap(Appointment::getId, Function.identity()));

        List<StaffAppointmentResponse> mapped = idPage.getContent().stream()
                .map(byId::get)
                .map(AppointmentMapper::toStaffResponse)
                .toList();
        return PagedResponse.from(idPage, mapped);
    }

    // Get a single appointment
    @Transactional(readOnly = true)
    public StaffAppointmentResponse getAppointment(User actor, UUID appointmentId) {

        Appointment appointment = resolveAppointmentWithBranchCheck(actor, appointmentId);
        return AppointmentMapper.toStaffResponse(appointment);
    }

    // Update appointment status (Admin & Manager)
    @CacheEvict(value = "appointments", allEntries = true)
    @Transactional
    public StaffAppointmentResponse updateStatus(User actor, UUID appointmentId, AppointmentStatus newStatus) {
        if (!UPDATABLE_STATUSES.contains(newStatus)) {
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

        return AppointmentMapper.toStaffResponse(appointment);
    }

    // Cancel appointment (Admin & Manager)
    public StaffAppointmentResponse cancel(User actor, UUID appointmentId) {
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

        return AppointmentMapper.toStaffResponse(appointment);
    }


    // Private helper
    private Appointment resolveAppointmentWithBranchCheck(User actor, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findByIdWithAssociations(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Appointment not found: "+ appointmentId));
        branchSecurityService.assertBranchAccess(actor, appointment.getBranch().getId());
        return appointment;
    }
}
