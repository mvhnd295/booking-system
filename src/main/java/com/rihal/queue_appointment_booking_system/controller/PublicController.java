package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.BranchResponse;
import com.rihal.queue_appointment_booking_system.dto.response.ServiceTypeResponse;
import com.rihal.queue_appointment_booking_system.dto.response.SlotResponse;
import com.rihal.queue_appointment_booking_system.service.PublicService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
@Tag(name = "Public", description = "Publicly accessible endpoints — no authentication required")
@SecurityRequirements
public class PublicController {
    private final PublicService publicService;

    // GET /api/public/branches
    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches() {
        List<BranchResponse> branches = publicService.getAllBranches();
        return ResponseEntity.ok(ApiResponse.success("Branches retrieved", branches));
    }

    // GET /api/public/branches/{branchId}
    @GetMapping("/branches/{branchId}")
    public ResponseEntity<ApiResponse<BranchResponse>> getBranch(
            @PathVariable UUID branchId
            ) {
        BranchResponse branch = publicService.getBranchById(branchId);
        return ResponseEntity.ok(ApiResponse.success("Branch retrieved", branch));
    }

    // GET /api/public/branches/{branchId}/services
    @GetMapping("/branches/{branchId}/services")
    public ResponseEntity<ApiResponse<List<ServiceTypeResponse>>> getServices(
            @PathVariable UUID branchId
    ) {
        List<ServiceTypeResponse> services = publicService.getServicesByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success("Services retrieved", services));
    }

    // GET /api/public/branches/{branchId}/services/{serviceId}
    @GetMapping("/branches/{branchId}/services/{serviceId}")
    public ResponseEntity<ApiResponse<ServiceTypeResponse>> getService(
            @PathVariable UUID serviceId,
            @PathVariable UUID branchId
    ) {
        ServiceTypeResponse service = publicService.getServiceById(serviceId, branchId);
        return ResponseEntity.ok(ApiResponse.success("Service retrieved", service));
    }

    // GET /api/public/branches/{branchId}/services/{serviceId}/slots
    @GetMapping("/branches/{branchId}/services/{serviceId}/slots")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getAvailableSlots(
            @PathVariable UUID serviceId,
            @PathVariable UUID branchId,
            @RequestParam(required = false)LocalDate date
            ) {
        List<SlotResponse> slots = publicService.getAvailableSlots(branchId, serviceId, date);
        return ResponseEntity.ok(ApiResponse.success("Slots retrieved", slots));
    }

    // GET /api/public/branches/{branchId}/services/{serviceId}/slots/{slotId}
}
