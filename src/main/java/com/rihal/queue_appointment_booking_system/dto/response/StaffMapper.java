package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.Staff;

import java.util.List;

public class StaffMapper {
    private StaffMapper() {}

    public static StaffResponse toStaffResponse(Staff s) {
        List<StaffResponse.ServiceTypeInfo> services = s.getStaffServiceTypes().stream()
                .map(sst -> new StaffResponse.ServiceTypeInfo(
                        sst.getServiceType().getId(),
                        sst.getServiceType().getName()))
                .toList();
        return new StaffResponse(
                s.getId(),
                s.getUsername(),
                s.getEmail(),
                s.getFullName(),
                s.getPhone(),
                s.getRole().getName().name(),
                s.isActive(),
                s.getBranch().getId(),
                s.getBranch().getName(),
                services
        );
    }
}
