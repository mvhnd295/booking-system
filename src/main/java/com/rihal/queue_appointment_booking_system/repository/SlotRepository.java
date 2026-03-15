package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<Slot, UUID> {
    Optional<Slot> findBySeedId(String seedId);
    boolean existsBySeedId(String seedId);

    // Using pessimistic locking on the slot row to prevent race conditions
    // During validation, the DB will lock the row until the transaction commits
    // This prevents double-booking on the DB level instead of JVM memory (works for distributed systems)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findByIdWithLock(@Param("id") UUID id);

    // Active not soft-deleted slots only used in public listing
    @Query("""
            SELECT s FROM Slot s
            WHERE s.branch.id = :branchId 
                AND s.serviceType.id = :serviceTypeId
                AND s.deleted = false AND s.active = true
                AND s.startAt >= :from 
                ORDER BY s.startAt ASC
            """)
    List<Slot> findAvailableSlots(@Param("branchId") UUID branchId,
                                  @Param("serviceTypeId") UUID serviceTypeId,
                                  @Param("from")LocalDateTime from);

    // List branch-specific slots for branch managers
    List<Slot> findAllByBranchId(UUID branchId);

    // Cleanup-job, soft deleted slots past retention period
    @Query("""
            SELECT s FROM Slot s
            WHERE s.deleted = true
            AND s.deletedAt < :cutoff
            """)
    List<Slot> findExpiredSoftDeletedSlots(@Param("cutoff") LocalDateTime cutoff);

}
