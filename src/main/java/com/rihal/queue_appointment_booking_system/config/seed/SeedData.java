package com.rihal.queue_appointment_booking_system.config.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

// --- Top Level -----------------------------------------------------------------
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeedData(
        UsersBlock users,
        List<BranchSeed> branches,
        @JsonProperty("service_types") List<ServiceTypeSeed> serviceTypes,
        @JsonProperty("staff_service_types") List<StaffServiceTypeSeed> staffServiceTypes,
        List<SlotSeed> slots,
        List<AppointmentSeed> appointments,
        @JsonProperty("audit_logs") List<AuditLogSeed> auditLogs
) {
    // ── Users block ───────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsersBlock(
            List<UserSeed> admin,
            @JsonProperty("branch_managers") List<UserSeed> branchManagers,
            List<UserSeed> staff,
            List<UserSeed> customers
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserSeed(
            String id,
            String username,
            String password,
            String role,
            @JsonProperty("full_name") String fullName,
            String email,
            String phone,
            @JsonProperty("branch_id") String branchId,
            @JsonProperty("is_active") boolean isActive
    ) {}

    // ── Branch ────────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BranchSeed(
            String id,
            String name,
            String city,
            String address,
            String timezone,
            @JsonProperty("is_active") boolean isActive
    ) {}

    // ── ServiceType ───────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServiceTypeSeed(
            String id,
            @JsonProperty("branch_id") String branchId,
            String name,
            String description,
            @JsonProperty("duration_minutes") int durationMinutes,
            @JsonProperty("is_active") boolean isActive
    ) {}

    // ── StaffServiceType ──────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StaffServiceTypeSeed(
            @JsonProperty("staff_id") String staffId,
            @JsonProperty("service_type_id") String serviceTypeId
    ) {}

    // ── Slot ──────────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SlotSeed(
            String id,
            @JsonProperty("branch_id") String branchId,
            @JsonProperty("service_type_id") String serviceTypeId,
            @JsonProperty("staff_id") String staffId,
            @JsonProperty("start_at") String startAt,
            @JsonProperty("end_at") String endAt,
            int capacity,
            @JsonProperty("is_active") boolean isActive
    ) {}

    // ── Appointment ───────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppointmentSeed(
            String id,
            @JsonProperty("customer_id") String customerId,
            @JsonProperty("branch_id") String branchId,
            @JsonProperty("service_type_id") String serviceTypeId,
            @JsonProperty("slot_id") String slotId,
            @JsonProperty("staff_id") String staffId,
            String status,
            @JsonProperty("created_at") String createdAt
    ) {}

    // ── AuditLog ──────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AuditLogSeed(
            String id,
            @JsonProperty("actor_id") String actorId,
            @JsonProperty("actor_role") String actorRole,
            @JsonProperty("action_type") String actionType,
            @JsonProperty("entity_type") String entityType,
            @JsonProperty("entity_id") String entityId,
            String timestamp,
            Map<String, Object> metadata
    ) {}
}
