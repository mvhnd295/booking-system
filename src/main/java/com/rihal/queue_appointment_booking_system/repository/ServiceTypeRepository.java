package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, UUID> {
    Optional<ServiceType> findBySeedId(String seedId);
    boolean existsBySeedId(String seedId);
    List<ServiceType> findByBranchId(UUID branchId);
    List<ServiceType> findByBranchIdAndActiveTrue(UUID branchId);
    Optional<ServiceType> findByIdAndBranchId(UUID serviceTypeId, UUID branchId);

    // Batch lookup — fetches only IDs that also belong to the given branch
    @Query("SELECT s FROM ServiceType s WHERE s.id IN :ids AND s.branch.id = :branchId")
    List<ServiceType> findAllByIdInAndBranchId(
            @Param("ids") List<UUID> ids,
            @Param("branchId") UUID branchId);
}
