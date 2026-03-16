package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.StaffServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StaffServiceTypeRepository extends JpaRepository<StaffServiceType, StaffServiceType.StaffServiceTypeId> {
    boolean existsByStaffIdAndServiceTypeId(UUID staffId, UUID serviceTypeId);

    void deleteByStaffIdAndServiceTypeId(UUID staffId, UUID serviceTypeId);
}
