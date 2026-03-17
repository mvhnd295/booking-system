package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Optional<Appointment> findBySeedId(String seedId);
    boolean existsBySeedId(String seedId);

    // Customer - own appointments (list mine)
    List<Appointment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    // Admin - list all appointments
    List<Appointment> findAllByOrderByCreatedAtDesc();

    // Branch Manager - list branch-scoped appointments
    List<Appointment> findByBranchIdOrderByCreatedAtDesc(UUID branchId);

    // Staff - list assigned appointments
    List<Appointment> findByStaffIdOrderByCreatedAtDesc(UUID staffId);

//    List<Appointment> findByBranchId(UUID branchId);
//    List<Appointment> findByStaffId(UUID staffId);


    // Prevent double booking the same appointment by checking if customer already booked this exact slot
    boolean existsByCustomerIdAndSlotIdAndStatusNotIn(
            UUID customerId,
            UUID slotId,
            List<AppointmentStatus> excludedStatuses
    );

    // Count active appointments for a customer at a specific branch
    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.customer.id = :customerId
                AND a.branch.id = :branchId
                AND a.status NOT IN :excludedStatuses
            """)
    long countActiveByCustomerAndBranch(
            @Param("customerId") UUID customerId,
            @Param("branchId") UUID branchId,
            @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses
    );

    // Count appointments created today by a customer (for booking rate limit)
    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.customer.id = :customerId
                AND CAST(a.createdAt AS date) = CURRENT_DATE
            """)
    long countTodayBookingsByCustomer(@Param("customerId") UUID customerId);

    // Nullify slot reference upon hard-delete
    @Query("""
            UPDATE Appointment a
            SET a.slot = null
            WHERE a.slot.id = :slotId
            """)
    @Modifying
    void nullifySlotReference(@Param("slotId") UUID slotId);

    //  Paginated search queries

    @Query("""
            SELECT a FROM Appointment a
            WHERE (:term IS NULL OR TRIM(:term) = ''
                OR LOWER(a.customer.fullName) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(a.branch.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(a.serviceType.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(CAST(a.status AS string)) LIKE LOWER(CONCAT('%', TRIM(:term), '%')))
            """)
    Page<Appointment> searchAll(@Param("term") String term, Pageable pageable);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.branch.id = :branchId
                AND (:term IS NULL OR TRIM(:term) = ''
                    OR LOWER(a.customer.fullName) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(a.branch.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(a.serviceType.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(CAST(a.status AS string)) LIKE LOWER(CONCAT('%', TRIM(:term), '%')))
            """)
    Page<Appointment> searchByBranch(@Param("term") String term, @Param("branchId") UUID branchId, Pageable pageable);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.customer.id = :customerId
                AND (:term IS NULL OR TRIM(:term) = ''
                    OR LOWER(a.branch.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(a.serviceType.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(CAST(a.status AS string)) LIKE LOWER(CONCAT('%', TRIM(:term), '%')))
            """)
    Page<Appointment> searchByCustomer(@Param("term") String term, @Param("customerId") UUID customerId, Pageable pageable);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.staff.id = :staffId
                AND (:term IS NULL OR TRIM(:term) = ''
                    OR LOWER(a.customer.fullName) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(a.serviceType.name) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                    OR LOWER(CAST(a.status AS string)) LIKE LOWER(CONCAT('%', TRIM(:term), '%')))
            """)
    Page<Appointment> searchByStaff(@Param("term") String term, @Param("staffId") UUID staffId, Pageable pageable);

    // Queue position
    @Query("""
        SELECT COUNT(a) FROM Appointment a
        WHERE a.slot.id = :slotId
          AND a.status IN :activeStatuses
          AND a.createdAt < :createdAt
        """)
    long countAheadInQueue(
            @Param("slotId") UUID slotId,
            @Param("activeStatuses") List<AppointmentStatus> activeStatuses,
            @Param("createdAt") LocalDateTime createdAt);

    @Query("""
        SELECT COUNT(a) FROM Appointment a
        WHERE a.slot.id = :slotId
          AND a.status IN :activeStatuses
        """)
    long countTotalInQueue(
            @Param("slotId") UUID slotId,
            @Param("activeStatuses") List<AppointmentStatus> activeStatuses);
}
