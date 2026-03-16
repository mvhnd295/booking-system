package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.Slot;

public class SlotMapper {
    private SlotMapper() {}

    public static SlotManagementResponse toResponse(Slot s) {
        return new SlotManagementResponse(
                s.getId(),
                s.getBranch().getId(),
                s.getBranch().getName(),
                s.getServiceType().getId(),
                s.getServiceType().getName(),
                s.getStaff() != null ? s.getStaff().getId() : null,
                s.getStaff() != null ? s.getStaff().getFullName() : null,
                s.getStartAt(),
                s.getEndAt(),
                s.getCapacity(),
                s.getBooked(),
                s.getCapacity() - s.getBooked(),
                s.isActive(),
                s.isDeleted(),
                s.getDeletedAt(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
