package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.domain.entity.Customer;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.response.CustomerMapper;
import com.rihal.queue_appointment_booking_system.dto.response.CustomerResponse;
import com.rihal.queue_appointment_booking_system.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerManagementService {

    private final CustomerRepository customerRepo;

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers() {
        return customerRepo
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found: "
                        + customerId
                ));
        return CustomerMapper.toResponse(customer);
    }
}
