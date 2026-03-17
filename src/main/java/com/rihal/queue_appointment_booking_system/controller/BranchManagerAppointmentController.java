package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateAppointmentStatusRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffAppointmentResponse;
import com.rihal.queue_appointment_booking_system.service.AppointmentManagementService;
import com.rihal.queue_appointment_booking_system.service.BranchSecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@PreAuthorize("hasRole(BRANCH_MANAGER)")
@RequiredArgsConstructor
@RequestMapping("/api/manager/appointments")
public class BranchManagerAppointmentController {

    private final AppointmentManagementService appointmentManagementService;
    private final BranchSecurityService branchSecurityService;

    // GET /api/manager/appointments - list branch-scoped appointments
    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffAppointmentResponse>>> list(
            @AuthenticationPrincipal User actor
            ) {
        UUID branchId = branchSecurityService.getManagerBranchId(actor);
        List<StaffAppointmentResponse> appointments =
                appointmentManagementService.listAppointments(actor, branchId);
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved.", appointments));
    }

    // GET /api/manager/appointments/{id} - get an appointment branch-scoped
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> getOne(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        StaffAppointmentResponse appointment = appointmentManagementService.getAppointment(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved.", appointment));
    }

    // PATCH /api/manager/appointments/{id}/status
    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> updateStatus(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
            ) {
        AppointmentStatus newStatus = request.status();
        StaffAppointmentResponse appointment = appointmentManagementService.updateStatus(actor, id, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated.", appointment));
    }

    // PATCH /api/manager/appointments/{id}/cancel
    @GetMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> cancel(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        StaffAppointmentResponse response = appointmentManagementService.cancel(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled.", response));
    }
}
