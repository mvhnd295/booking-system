package com.rihal.queue_appointment_booking_system.repository;

import com.rihal.queue_appointment_booking_system.domain.entity.Customer;
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
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findBySeedId(String seedId);
    boolean existsBySeedId(String seederId);
    List<Customer> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT c FROM Customer c
            WHERE (:term IS NULL OR TRIM(:term) = ''
                OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(c.email) LIKE LOWER(CONCAT('%', TRIM(:term), '%'))
                OR LOWER(c.username) LIKE LOWER(CONCAT('%', TRIM(:term), '%')))
            """)
    Page<Customer> searchCustomers(@Param("term") String term, Pageable pageable);
}
