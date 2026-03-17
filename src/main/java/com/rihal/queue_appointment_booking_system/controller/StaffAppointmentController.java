package com.rihal.queue_appointment_booking_system.controller;


import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateAppointmentStatusRequest;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateNoteRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffAppointmentResponse;
import com.rihal.queue_appointment_booking_system.service.StaffAppointmentService;
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
@RequestMapping("/api/staff/appointments")
@PreAuthorize("hasRole('STAFF')")
public class StaffAppointmentController {
    private final StaffAppointmentService staffAppointmentService;

    // GET /api/staff/appointments - List staff's assigned appointments
    @GetMapping
    public ResponseEntity<ApiResponse<List<StaffAppointmentResponse>>> list(
            @AuthenticationPrincipal User actor
            ) {
        List<StaffAppointmentResponse> appointments = staffAppointmentService.listMyAppointments(actor);
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved.", appointments));
    }

    // GET /api/staff/appointments/{id} - Get assigned appointment
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> getOne(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
            ) {
        StaffAppointmentResponse appointment = staffAppointmentService.getMyAppointment(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved.", appointment));
    }

    // PATCH /api/staff/appointments/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> updateStatus(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
            ) {
        StaffAppointmentResponse response =
                staffAppointmentService.updateStatus(actor, id, request.status());
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated.", response));
    }

    // PATCH /api/staff/appointments/{id}/notes
    @PatchMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<StaffAppointmentResponse>> addNote(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNoteRequest request
            ) {
        StaffAppointmentResponse response =
                staffAppointmentService.addNote(actor, id, request.note());
        return ResponseEntity.ok(ApiResponse.success("Note added.", response));
    }
}
