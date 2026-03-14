package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;
import com.rihal.queue_appointment_booking_system.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<Appointment> findByBranchId(UUID branchId);
    List<Appointment> findByStaffId(UUID staffId);
    List<Appointment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

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
}
