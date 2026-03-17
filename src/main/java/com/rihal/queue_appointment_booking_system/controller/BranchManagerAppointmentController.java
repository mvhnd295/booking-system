package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateAppointmentStatusRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffAppointmentResponse;
import com.rihal.queue_appointment_booking_system.service.AppointmentManagementService;
import com.rihal.queue_appointment_booking_system.service.BranchSecurityService;
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
@PreAuthorize("hasRole(BRANCH_MANAGER)")
@RequiredArgsConstructor
@RequestMapping("/api/manager/appointments")
@Tag(name = "Manager \u2014 Appointments", description = "Branch-scoped appointment management")
public class BranchManagerAppointmentController {

    private final AppointmentManagementService appointmentManagementService;
    private final BranchSecurityService branchSecurityService;

    // GET /api/manager/appointments - list branch-scoped appointments
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StaffAppointmentResponse>>> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String term
            ) {
        UUID branchId = branchSecurityService.getManagerBranchId(actor);
        PagedResponse<StaffAppointmentResponse> appointments =
                appointmentManagementService.listAppointments(actor, branchId, term,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
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
