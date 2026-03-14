package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.Branch;

import java.util.UUID;

public record BranchResponse(
        UUID id,
        String name,
        String city,
        String address,
        String timezone
) {
    public static BranchResponse from(Branch branch) {
        return new BranchResponse(
                branch.getId(),
                branch.getName(),
                branch.getCity(),
                branch.getAddress(),
                branch.getTimezone()
        );
    }
}
