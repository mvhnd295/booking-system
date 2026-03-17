package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.CustomerResponse;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.service.CustomerManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BRANCH_MANAGER')")
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "List and view customers \u2014 Manager and Admin")
public class CustomerManagementController {
    private final CustomerManagementService customerService;

    // GET /api/customers
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String term
    ) {
        PagedResponse<CustomerResponse> customers = customerService.listCustomers(
                term, PageRequest.of(page, size, Sort.by("createdAt").descending()));
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
