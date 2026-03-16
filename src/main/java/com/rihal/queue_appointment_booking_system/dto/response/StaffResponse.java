package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.ServiceType;

import java.util.List;
import java.util.UUID;

public record StaffResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String phone,
        String role,
        boolean active,
        UUID branchId,
        String branchName,
        List<ServiceTypeInfo> assignedServices
) {
    public record ServiceTypeInfo(UUID id, String name) {}
}
