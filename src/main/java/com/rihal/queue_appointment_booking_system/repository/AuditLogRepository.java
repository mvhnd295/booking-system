package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Optional<AuditLog> findBySeedId(String seedId);
    boolean existsBySeedId(String seedId);
    // Manager views logs based on their branch
    List<AuditLog> findByBranchIdOrderByTimestampDesc(UUID branchId);
    // Admin views all logs
    List<AuditLog> findAllByOrderByTimestampDesc();
}
