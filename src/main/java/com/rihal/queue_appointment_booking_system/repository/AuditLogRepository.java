package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.AuditLog;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Optional<AuditLog> findBySeedId(String seedId);
    boolean existsBySeedId(String seedId);

    // Count reschedules performed today by a specific actor (for rate limiting)
    @Query("""
            SELECT COUNT(l) FROM AuditLog l
            WHERE l.actorId = :actorId
                AND l.action = :action
                AND CAST(l.timestamp AS date) = CURRENT_DATE
            """)
    long countTodayByActorAndAction(
            @Param("actorId") UUID actorId,
            @Param("action") AuditAction action
    );

    // Manager views logs based on their branch (branch-scoped)
    List<AuditLog> findByBranchIdOrderByTimestampDesc(UUID branchId);

    // Admin views all logs from newest to oldest
    List<AuditLog> findAllByOrderByTimestampDesc();

    @Query("""
            SELECT l FROM AuditLog l
            WHERE (:term IS NULL OR TRIM(:term) = ''
                OR LOWER(CAST(l.action AS string)) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(CAST(l.actorRole AS string)) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR (l.branch IS NOT NULL AND LOWER(l.branch.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))))
            """)
    Page<AuditLog> searchLogs(@Param("term") String term, Pageable pageable);
}
