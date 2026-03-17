package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateAppointmentStatusRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentResponse;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffAppointmentResponse;
import com.rihal.queue_appointment_booking_system.service.AppointmentManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/appointments")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin \u2014 Appointments", description = "System-wide appointment management")
public class AdminAppointmentController {

    private final AppointmentManagementService appService;

    // GET /api/admin/appointments
    // - branchId is optional to be passed as query param if null it returns all appointments
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StaffAppointmentResponse>>> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String term
    ) {
        PagedResponse<StaffAppointmentResponse> appointments =
                appService.listAppointments(actor, branchId, term,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved.", appointments));
    }

    // GET /api/admin/appointments/{id} - get any appointment in any branch
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> getOne(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        StaffAppointmentResponse appointment = appService.getAppointment(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved.", appointment));
    }

    // PATCH /api/admin/appointments/{id}/status
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> updateStatus(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        AppointmentStatus newStatus = request.status();
        StaffAppointmentResponse appointment = appService.updateStatus(actor, id, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated.", appointment));
    }

    // PATCH /api/admin/appointments/{id}/cancel
    @GetMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> cancel(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        StaffAppointmentResponse response = appService.cancel(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled.", response));
    }
}
