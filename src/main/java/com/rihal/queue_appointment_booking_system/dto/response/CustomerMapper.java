package com.rihal.queue_appointment_booking_system.dto.response;


import com.rihal.queue_appointment_booking_system.domain.entity.Customer;

public class CustomerMapper {
    private CustomerMapper() {}

    public static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getId(),
                c.getUsername(),
                c.getEmail(),
                c.getFullName(),
                c.getPhone(),
                c.isActive(),
                c.getIdImagePath()
        );
    }
}
