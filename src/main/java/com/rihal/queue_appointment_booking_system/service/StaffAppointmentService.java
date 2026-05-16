package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.audit.AuditService;
import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentMapper;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffAppointmentResponse;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffAppointmentService {
    private final AppointmentRepository appRepo;
    private final AuditService auditService;

    private final Set<AppointmentStatus> ALLOWED_STATUS_UPDATES =
            Set.of(AppointmentStatus.CHECKED_IN, AppointmentStatus.NO_SHOW, AppointmentStatus.COMPLETED);

    // List assigned appointments
    @Cacheable(value = "staffAppointments", key = "#actor.id + '_' + #term + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResponse<StaffAppointmentResponse> listMyAppointments(User actor, String term, Pageable pageable) {
        // Two-query pattern: 1) fetch IDs with SQL-level pagination
        Page<UUID> idPage = appRepo.findIdsByStaff(term, actor.getId(), pageable);

        if (idPage.isEmpty()) {
            return PagedResponse.from(idPage, List.of());
        }

        // 2) Fetch full entities with all associations eagerly loaded
        List<Appointment> appointments = appRepo.findAllWithAssociationsByIds(idPage.getContent());
        Map<UUID, Appointment> byId = appointments.stream()
                .collect(Collectors.toMap(Appointment::getId, Function.identity()));

        List<StaffAppointmentResponse> mapped = idPage.getContent().stream()
                .map(byId::get)
                .map(AppointmentMapper::toStaffResponse)
                .toList();
        return PagedResponse.from(idPage, mapped);
    }
    // Get one assigned appointment
    @Transactional(readOnly = true)
    public StaffAppointmentResponse getMyAppointment(User actor, UUID appointmentId) {
        return AppointmentMapper.toStaffResponse(resolveStaffAppointment(actor, appointmentId));
    }

    // Update assigned appointment status
    @CacheEvict(value = "staffAppointments", allEntries = true)
    @Transactional
    public StaffAppointmentResponse updateStatus(User actor, UUID appointmentId,
                                                 AppointmentStatus newStatus) {
        if (!ALLOWED_STATUS_UPDATES.contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Status must be one of: CHECKED_IN, NO_SHOW, COMPLETED.");
        }

        Appointment appointment = resolveStaffAppointment(actor, appointmentId);
        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(newStatus);
        appointment = appRepo.saveAndFlush(appointment);

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

    // Add internal note
    @Transactional
    public StaffAppointmentResponse addNote(User actor, UUID appointmentId, String note) {
        Appointment appointment = resolveStaffAppointment(actor, appointmentId);
        if (appointment.getInternalNotes() == null) {
            appointment.setInternalNotes(new ArrayList<>());
        }
        appointment.getInternalNotes().add(note);
        appointment = appRepo.saveAndFlush(appointment);
        return AppointmentMapper.toStaffResponse(appointment);
    }

    // helper
    // resolves an appointment and makes sure It's assigned to staff member
    private Appointment resolveStaffAppointment(User actor, UUID appointmentId) {
        Appointment appointment = appRepo.findByIdWithAssociations(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Appointment not found."));
        if (appointment.getStaff() == null
        || !appointment.getStaff().getId().equals(actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This appointment is not assigned to this staff member.");
        }
        return appointment;
    }
}
