package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.request.MaxPerDayRequest;
import com.rihal.queue_appointment_booking_system.dto.request.RetentionDayRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.service.AppConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin \u2014 Configuration", description = "System configuration and cleanup \u2014 Admin only")
public class AppConfigController {

    private final AppConfigService appConfigService;

    //  Retention days

    // GET /api/config/retention-days
    @GetMapping("/retention-days")
    public ResponseEntity<ApiResponse<Integer>> getRetentionDays() {
        int days = appConfigService.retentionDays();
        return ResponseEntity.ok(ApiResponse.success("Retention period retrieved.", days));
    }

    // PUT /api/config/retention-days
    @PutMapping("/retention-days")
    public ResponseEntity<ApiResponse<Integer>> updateRetentionDays(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody RetentionDayRequest request
    ) {
        int updated = appConfigService.updateRetentionPeriod(request.days(), actor);
        return ResponseEntity.ok(
                ApiResponse.success("Retention period updated to " + updated + " days.", updated));
    }

    // Max bookings per day

    // GET /api/config/max-bookings-per-day
    @GetMapping("/max-bookings-per-day")
    public ResponseEntity<ApiResponse<Integer>> getMaxBookingsPerDay() {
        int max = appConfigService.getMaxBookingsPerDay();
        return ResponseEntity.ok(ApiResponse.success("Max bookings per day retrieved.", max));
    }

    // PUT /api/config/max-bookings-per-day
    @PutMapping("/max-bookings-per-day")
    public ResponseEntity<ApiResponse<Integer>> updateMaxBookingsPerDay(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody MaxPerDayRequest request
    ) {
        int updated = appConfigService.updateMaxBookingsPerDay(request.max(), actor);
        return ResponseEntity.ok(
                ApiResponse.success("Max bookings per day updated to " + updated + ".", updated));
    }

    // Max reschedules per day

    // GET /api/config/max-reschedules-per-day
    @GetMapping("/max-reschedules-per-day")
    public ResponseEntity<ApiResponse<Integer>> getMaxReschedulesPerDay() {
        int max = appConfigService.getMaxReschedulesPerDay();
        return ResponseEntity.ok(ApiResponse.success("Max reschedules per day retrieved.", max));
    }

    // PUT /api/config/max-reschedules-per-day
    @PutMapping("/max-reschedules-per-day")
    public ResponseEntity<ApiResponse<Integer>> updateMaxReschedulesPerDay(
            @AuthenticationPrincipal User actor,
            @Valid @RequestBody MaxPerDayRequest request
    ) {
        int updated = appConfigService.updateMaxReschedulesPerDay(request.max(), actor);
        return ResponseEntity.ok(
                ApiResponse.success("Max reschedules per day updated to " + updated + ".", updated));
    }
}
