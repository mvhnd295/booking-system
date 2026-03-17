package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.request.BookAppointmentRequest;
import com.rihal.queue_appointment_booking_system.dto.request.RescheduleAppointmentRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentResponse;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.dto.response.QueuePositionResponse;
import com.rihal.queue_appointment_booking_system.service.CustomerAppointmentService;
import com.rihal.queue_appointment_booking_system.service.QueuePositionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/appointments")
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "Customer \u2014 Appointments", description = "Customer self-service booking and appointment management")
public class CustomerAppointmentController {
    private final CustomerAppointmentService appointmentService;
    private final QueuePositionService queuePositionService;

    // POST /api/customer/appointments
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(
            @AuthenticationPrincipal User actor,
            @RequestParam @NotNull(message = "Slot ID is required.") UUID slotId,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment
            ) {
        AppointmentResponse response = appointmentService.book(actor, slotId, attachment);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment Booked successfully.", response));
    }

    // GET /api/customer/appointments
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentResponse>>> listMine(
            @AuthenticationPrincipal User actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String term
    ) {
        PagedResponse<AppointmentResponse> appointments = appointmentService.listMine(
                actor, term, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved.", appointments));
    }

    // GET /api/customer/appointments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        AppointmentResponse appointment = appointmentService.getOne(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved.", appointment));
    }

    // PATCH /api/customer/appointments/{id}/cancel
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        AppointmentResponse cancelledAppointment = appointmentService.cancel(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled.", cancelledAppointment));
    }

    // PATCH /api/customer/appointments/{id}/reschedule
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request
            ) {
        AppointmentResponse rescheduledAppointment = appointmentService.reschedule(actor, id, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled.", rescheduledAppointment));
    }

    // GET /api/customer/appointments/{id}/queue-position
    @GetMapping("/{id}/queue-position")
    public ResponseEntity<ApiResponse<QueuePositionResponse>> getQueuePosition(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success("Queue position retrieved.",
                queuePositionService.getQueuePosition(actor, id)));
    }
}
