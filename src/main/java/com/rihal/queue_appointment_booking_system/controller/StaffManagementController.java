package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.request.AssignServiceRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffResponse;
import com.rihal.queue_appointment_booking_system.service.StaffManagementService;
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
@PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
@RequestMapping("/api/staff")
@Tag(name = "Staff Management", description = "List staff and manage service assignments \u2014 Manager and Admin")
public class StaffManagementController {

    private final StaffManagementService staffService;

    // GET /api/staff
    // Admin: all staff | BM: staff of their branch
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<StaffResponse>>> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String term
            ) {
        PagedResponse<StaffResponse> staffList = staffService.listStaff(
                actor, term, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(
                "Staff retrieved.",
                staffList
        ));
    }

    // POST /api/staff/{staffId}/services
    // Assign one or more service to a staff member
    @PostMapping("/{staffId}/services")
    public ResponseEntity<ApiResponse<StaffResponse>> assign(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID staffId,
            @Valid @RequestBody AssignServiceRequest request
            ) {
        StaffResponse response = staffService.assignService(actor, staffId, request.serviceTypeIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Service(s) assigned to staff member.",
                response
        ));
    }

    // DELETE /api/staff/{staffId}/services/{serviceTypeId}
    @DeleteMapping("/{staffId}/{serviceTypeId}")
    public ResponseEntity<ApiResponse<StaffResponse>> remove(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID staffId,
            @PathVariable UUID serviceTypeId
    ) {
        StaffResponse response = staffService.removeService(actor, staffId, serviceTypeId);
        return ResponseEntity.ok(ApiResponse.success(
                "Service removed from staff member.",
                response
        ));
    }
}
