package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.Slot;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlotResponse(
        UUID id,
        UUID branchId,
        UUID serviceTypeId,
        UUID staffId,
        String staffName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        int capacity,
        int booked,
        int available
) {
    public static SlotResponse from(Slot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getBranch().getId(),
                slot.getServiceType().getId(),
                slot.getStaff() != null ? slot.getStaff().getId() : null,
                slot.getStaff() != null ? slot.getStaff().getFullName() : null,
                slot.getStartAt(),
                slot.getEndAt(),
                slot.getCapacity(),
                slot.getBooked(),
                slot.getCapacity() - slot.getBooked()
        );
    }
}
