package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateAppointmentStatusRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.AppointmentResponse;
import com.rihal.queue_appointment_booking_system.service.AppointmentManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/appointments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppointmentController {

    private final AppointmentManagementService appService;

    // GET /api/admin/appointments
    // - branchId is optional to be passed as query param if null it returns all appointments
    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) UUID branchId
    ) {
        List<AppointmentResponse> appointments =
                appService.listAppointments(actor, branchId);
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved.", appointments));
    }

    // GET /api/admin/appointments/{id} - get any appointment in any branch
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getOne(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        AppointmentResponse appointment = appService.getAppointment(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved.", appointment));
    }

    // PATCH /api/admin/appointments/{id}/status
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        AppointmentStatus newStatus = request.status();
        AppointmentResponse appointment = appService.updateStatus(actor, id, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated.", appointment));
    }

    // PATCH /api/admin/appointments/{id}/cancel
    @GetMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        AppointmentResponse response = appService.cancel(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled.", response));
    }
}
