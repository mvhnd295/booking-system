package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.StaffServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StaffServiceTypeRepository extends JpaRepository<StaffServiceType, StaffServiceType.StaffServiceTypeId> {
    boolean existsByStaffIdAndServiceTypeId(UUID staffId, UUID serviceTypeId);

    void deleteByStaffIdAndServiceTypeId(UUID staffId, UUID serviceTypeId);

    // Batch check — returns only the IDs that are already assigned, avoiding N individual EXISTS queries
    @Query("""
            SELECT sst.serviceType.id FROM StaffServiceType sst
            WHERE sst.staff.id = :staffId
            AND sst.serviceType.id IN :serviceTypeIds
            """)
    List<UUID> findAssignedServiceTypeIds(
            @Param("staffId") UUID staffId,
            @Param("serviceTypeIds") List<UUID> serviceTypeIds);
}
