package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // Nullify slot reference upon hard-delete
    @Query("""
            UPDATE Appointment a
            SET a.slot = null
            WHERE a.slot.id = :slotId
            """)
    @Modifying
    void nullifySlotReference(@Param("slotId") UUID slotId);
}
