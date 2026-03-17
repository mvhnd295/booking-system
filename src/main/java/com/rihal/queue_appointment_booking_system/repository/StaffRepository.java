package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.Staff;
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
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    Optional<Staff> findBySeedId(String seedId);
    boolean existsBySeedId(String seedId);

    List<Staff> findAllByBranchId(UUID branchId);

    @Query("""
            SELECT s FROM Staff s
            WHERE (:term IS NULL OR TRIM(:term) = ''
                OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(s.email) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(s.branch.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%')))
            """)
    Page<Staff> searchAllStaff(@Param("term") String term, Pageable pageable);

    @Query("""
            SELECT s FROM Staff s
            WHERE s.branch.id = :branchId
                AND (:term IS NULL OR TRIM(:term) = ''
                    OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(s.email) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(s.branch.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%')))
            """)
    Page<Staff> searchStaffByBranch(@Param("term") String term, @Param("branchId") UUID branchId, Pageable pageable);
}
