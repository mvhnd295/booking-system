package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.request.SlotRequest;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateSlotRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.SlotManagementResponse;
import com.rihal.queue_appointment_booking_system.service.SlotManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slots")
@PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
public class SlotManagementController {

    private final SlotManagementService slotService;

    /**
     * GET /api/slots?branchId=...
     * Admin: all slots (optionally filtered by branchId)
     * Manager: always scoped to own branch
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SlotManagementResponse>>> list(
            @AuthenticationPrincipal User actor,
            @RequestParam(required = false) UUID branchId
    ) {
        List<SlotManagementResponse> slots = slotService.listSlots(actor, branchId);
        return ResponseEntity.ok(ApiResponse.success("Slots retrieved.", slots));
    }

    /**
     * POST /api/slots
     * Body: array of SlotRequest — send an array of one for single creation
     */
    @PostMapping
    public ResponseEntity<ApiResponse<List<SlotManagementResponse>>> create(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody List<SlotRequest> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("At least one slot is required."));
        }
        List<SlotManagementResponse> created = slotService.createSlots(actor, requests);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        created.size() + " slot(s) created successfully.", created));
    }

    /**
     * PUT /api/slots/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SlotManagementResponse>> update(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSlotRequest request
    ) {
        SlotManagementResponse response = slotService.updateSlot(actor, id, request);
        return ResponseEntity.ok(ApiResponse.success("Slot updated.", response));
    }

    /**
     * DELETE /api/slots/{id}  — soft delete
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID id
    ) {
        slotService.softDeleteSlot(actor, id);
        return ResponseEntity.ok(ApiResponse.success("Slot deleted.", null));
    }
}
