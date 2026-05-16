package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.audit.AuditService;
import com.rihal.queue_appointment_booking_system.domain.entity.*;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.dto.request.RescheduleAppointmentRequest;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentMapper;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentResponse;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import com.rihal.queue_appointment_booking_system.repository.AttachmentRepository;
import com.rihal.queue_appointment_booking_system.repository.CustomerRepository;
import com.rihal.queue_appointment_booking_system.repository.SlotRepository;
import com.rihal.queue_appointment_booking_system.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerAppointmentService {

    // List of Appointment statuses that cant be rescheduled
    private static final List<AppointmentStatus> INACTIVE_STATUSES =
            List.of(
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.COMPLETED,
                    AppointmentStatus.NO_SHOW
            );
    // Minimum time before slot start where booking is allowed
    private static final int BOOKING_LEAD_TIME = 30;

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final CustomerRepository customerRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    // ── Book ──────────────────────────────────────────────────────────────────

    @CacheEvict(value = "customerAppointments", allEntries = true)
    @Transactional
    public AppointmentResponse book(User actor, UUID slotId, MultipartFile attachmentFile) {
        Customer customer = resolveCustomer(actor);

        // Rate limit check — booking
        rateLimitService.checkBookingLimit(customer.getId());

        Slot slot = resolveSlot(slotId);

        validateBooking(customer, slot);

        // Attachment is optional so can be null
        Attachment attachment = null;
        if (attachmentFile != null && !attachmentFile.isEmpty()) {
            String filePath = fileStorageService.storeAttachment(attachmentFile);
            attachment = new Attachment();
            attachment.setFilePath(filePath);
            attachment.setOriginalName(attachmentFile.getOriginalFilename());
            attachment.setMimeType(attachmentFile.getContentType());
            attachment.setSizeBytes(attachmentFile.getSize());
            attachmentRepository.save(attachment);
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setSlot(slot);
        appointment.setBranch(slot.getBranch());
        appointment.setServiceType(slot.getServiceType());
        appointment.setStaff(slot.getStaff());
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setAttachment(attachment);
        appointmentRepository.saveAndFlush(appointment);

        // Increment slot booked count
        slot.setBooked(slot.getBooked() + 1);
        slotRepository.save(slot);

        // Audit
        auditService.log(
                AuditAction.APPOINTMENT_BOOKED,
                actor,
                EntityType.APPOINTMENT,
                appointment.getId(),
                slot.getBranch(),
                Map.of("slotId", slot.getId().toString())
        );

        return AppointmentMapper.toResponse(appointment);
    }

    // ── List my appointments ─────────────────────────────────────────────────

    @Cacheable(value = "customerAppointments", key = "#actor.id + '_' + #term + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentResponse> listMine(User actor, String term, Pageable pageable) {
        // Two-query pattern: 1) fetch IDs with SQL-level pagination
        Page<UUID> idPage = appointmentRepository.findIdsByCustomer(term, actor.getId(), pageable);

        if (idPage.isEmpty()) {
            return PagedResponse.from(idPage, List.of());
        }

        // 2) Fetch full entities with all associations eagerly loaded
        List<Appointment> appointments = appointmentRepository.findAllWithAssociationsByIds(idPage.getContent());
        Map<UUID, Appointment> byId = appointments.stream()
                .collect(Collectors.toMap(Appointment::getId, Function.identity()));

        List<AppointmentResponse> mapped = idPage.getContent().stream()
                .map(byId::get)
                .map(AppointmentMapper::toResponse)
                .toList();
        return PagedResponse.from(idPage, mapped);
    }

    // ── Get one appointment ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AppointmentResponse getOne(User actor, UUID appointmentId) {
        Appointment appointment = resolveOwnAppointment(actor, appointmentId);
        return AppointmentMapper.toResponse(appointment);
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    @Transactional
    public AppointmentResponse cancel(User actor, UUID appointmentId) {
        Appointment appointment = resolveOwnAppointment(actor, appointmentId);

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalStateException(
                    "Only BOOKED appointments can be cancelled. Current status: " + appointment.getStatus()
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(appointment);

        // Decrement slot booked count if slot still exists
        Slot slot = appointment.getSlot();
        if (slot != null && slot.getBooked() > 0) {
            slot.setBooked(slot.getBooked() - 1);
            slotRepository.save(slot);
        }

        auditService.log(
                AuditAction.APPOINTMENT_CANCELLED,
                actor,
                EntityType.APPOINTMENT,
                appointment.getId(),
                appointment.getBranch()
        );

        return AppointmentMapper.toResponse(appointment);
    }

    // ── Reschedule ───────────────────────────────────────────────────────────

    @Transactional
    public AppointmentResponse reschedule(User actor, UUID appointmentId,
                                          RescheduleAppointmentRequest request) {
        // Rate limit check — reschedule
        rateLimitService.checkRescheduleLimit(actor.getId());

        Appointment appointment = resolveOwnAppointment(actor, appointmentId);

        // Any active status can be rescheduled
        if (INACTIVE_STATUSES.contains(appointment.getStatus())) {
            throw new IllegalStateException(
                    "Cannot reschedule an appointment with status: " + appointment.getStatus()
            );
        }

        if (request.newSlotId().equals(
                appointment.getSlot() != null ? appointment.getSlot().getId() : null)) {
            throw new IllegalArgumentException("New slot must be different from the current slot.");
        }

        Slot newSlot = resolveSlot(request.newSlotId());
        validateBooking(appointment.getCustomer(), newSlot);

        UUID oldSlotId = appointment.getSlot() != null ? appointment.getSlot().getId() : null;

        // Release old slot
        Slot oldSlot = appointment.getSlot();
        if (oldSlot != null && oldSlot.getBooked() > 0) {
            oldSlot.setBooked(oldSlot.getBooked() - 1);
            slotRepository.save(oldSlot);
        }

        // Assign new slot
        appointment.setSlot(newSlot);
        appointment.setBranch(newSlot.getBranch());
        appointment.setServiceType(newSlot.getServiceType());
        appointment.setStaff(newSlot.getStaff());
        // Keep the current status — reschedule doesn't change status
        appointmentRepository.saveAndFlush(appointment);

        newSlot.setBooked(newSlot.getBooked() + 1);
        slotRepository.save(newSlot);

        auditService.log(
                AuditAction.APPOINTMENT_RESCHEDULED,
                actor,
                EntityType.APPOINTMENT,
                appointment.getId(),
                newSlot.getBranch(),
                Map.of(
                        "oldSlotId", oldSlotId != null ? oldSlotId.toString() : "null",
                        "newSlotId", newSlot.getId().toString()
                )
        );

        return AppointmentMapper.toResponse(appointment);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Customer resolveCustomer(User actor) {
        return customerRepository.findById(actor.getId())
                .orElseThrow(() -> new IllegalStateException("Customer profile not found."));
    }

    private Slot resolveSlot(UUID slotId) {
        return slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));
    }

    private Appointment resolveOwnAppointment(User actor, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findByIdWithAssociations(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + appointmentId));

        if (!appointment.getCustomer().getId().equals(actor.getId())) {
            throw new SecurityException("You do not have access to this appointment.");
        }

        return appointment;
    }

    /**
     * Central booking validation — reused by both book() and reschedule().
     * Checks:
     *   1. Slot is not soft-deleted
     *   2. Slot is active
     *   3. Slot start is at least 30 minutes from now
     *   4. Slot is not full
     *   5. Customer hasn't already booked this exact slot
     *   6. Customer doesn't already have an active appointment at the same branch
     */
    private void validateBooking(Customer customer, Slot slot) {
        if (slot.isDeleted()) {
            throw new IllegalArgumentException("This slot is no longer available.");
        }

        if (!slot.isActive()) {
            throw new IllegalArgumentException("This slot is not active.");
        }

        if (slot.getStartAt().isBefore(LocalDateTime.now().plusMinutes(BOOKING_LEAD_TIME))) {
            throw new IllegalArgumentException(
                    "Slots must be booked at least " + BOOKING_LEAD_TIME + " minutes in advance."
            );
        }

        if (slot.getBooked() >= slot.getCapacity()) {
            throw new IllegalArgumentException("This slot is fully booked.");
        }

        boolean alreadyBookedSlot = appointmentRepository
                .existsByCustomerIdAndSlotIdAndStatusNotIn(
                        customer.getId(), slot.getId(), INACTIVE_STATUSES
                );
        if (alreadyBookedSlot) {
            throw new IllegalArgumentException("You have already booked this slot.");
        }

        long activeAtBranch = appointmentRepository.countActiveByCustomerAndBranch(
                customer.getId(), slot.getBranch().getId(), INACTIVE_STATUSES
        );
        if (activeAtBranch >= 1) {
            throw new IllegalArgumentException(
                    "You already have an active appointment at this branch. " +
                            "Please cancel or complete it before booking another."
            );
        }
    }

}
