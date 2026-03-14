package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Optional<Branch> findBySeedId(String seedId);
    boolean existsBySeedId(String seedId);
    List<Branch> findByActiveTrue();
    Optional<Branch> findByIdAndActiveTrue(UUID branchId);
}
