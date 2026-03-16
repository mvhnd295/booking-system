package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.request.RetentionDayRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.service.AppConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/config")
@PreAuthorize("hasRole('ADMIN')")
public class AppConfigController {

    private final AppConfigService appConfigService;

    // GET /api/admin/config/retention-days
    @GetMapping("/retention-days")
    public ResponseEntity<ApiResponse<Integer>> getRetentionDays() {
        int days = appConfigService.retentionDays();
        return ResponseEntity.ok(ApiResponse.success("Retention period retrieved.", days));
    }

    // PUT /api/admin/config/retention-days
    @PutMapping("/retention-days")
    public ResponseEntity<ApiResponse<Integer>> updateRetentionDays(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody RetentionDayRequest request
    ) {
        int updated = appConfigService.updateRetentionPeriod(request.days(), actor);
        return ResponseEntity.ok(
                ApiResponse.success("Retention period updated to " + updated + " days.", updated));
    }
}
