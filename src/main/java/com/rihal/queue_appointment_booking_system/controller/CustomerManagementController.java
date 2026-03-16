package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.CustomerResponse;
import com.rihal.queue_appointment_booking_system.service.CustomerManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
@RequestMapping("/api/customers")
public class CustomerManagementController {
    private final CustomerManagementService customerService;

    // GET /api/customers
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> list() {
        List<CustomerResponse> customers = customerService.listCustomers();
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved.", customers));
    }

    // GET /api/customers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getOne(
            @PathVariable UUID id
    ) {
        CustomerResponse customer = customerService.getCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer retrieved.", customer));
    }
}
