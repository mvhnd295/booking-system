package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.domain.entity.Branch;
import com.rihal.queue_appointment_booking_system.domain.entity.ServiceType;
import com.rihal.queue_appointment_booking_system.dto.response.BranchResponse;
import com.rihal.queue_appointment_booking_system.dto.response.ServiceTypeResponse;
import com.rihal.queue_appointment_booking_system.dto.response.SlotResponse;
import com.rihal.queue_appointment_booking_system.repository.BranchRepository;
import com.rihal.queue_appointment_booking_system.repository.ServiceTypeRepository;
import com.rihal.queue_appointment_booking_system.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicService {
    private final BranchRepository branchRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final SlotRepository slotRepository;

    // ── Branches

    public List<BranchResponse> getAllBranches() {
        return branchRepository.findByActiveTrue()
                .stream()
                .map(BranchResponse::from)
                .toList();
    }
    // ── Branch by ID

    public BranchResponse getBranchById(UUID branchId) {
        Branch branch = branchRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
        return BranchResponse.from(branch);
    }

    // ── Services by Branch

    public List<ServiceTypeResponse> getServicesByBranch(UUID branchId) {
        // Verify branch exists and is active
        branchRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        return serviceTypeRepository.findByBranchIdAndActiveTrue(branchId)
                .stream()
                .map(ServiceTypeResponse::from)
                .toList();
    }
    // ── Service by ID

    public ServiceTypeResponse getServiceById(UUID serviceId, UUID branchId) {
        ServiceType serviceType = serviceTypeRepository.findByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found for this branch"));
        return ServiceTypeResponse.from(serviceType);
    }

    // ── Available Slots by Branch + Service

    public List<SlotResponse> getAvailableSlots(UUID branchId, UUID serviceTypeId, LocalDate date) {
        // Verify branch exists
        branchRepository.findByIdAndActiveTrue(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        // Verify service belongs to this branch
        serviceTypeRepository.findByIdAndBranchId(serviceTypeId, branchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Service not found for this branch"));

        // Check if date is passed
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);
            return slotRepository.findAvailableSlotsByDate(branchId, serviceTypeId, start, end)
                    .stream()
                    .filter(s -> !s.isFull())
                    .map(SlotResponse::from)
                    .toList();
        }

        // Only return future slots that are not full and not deleted
        return slotRepository.findAvailableSlots(branchId, serviceTypeId, LocalDateTime.now())
                .stream()
                .filter(slot -> !slot.isFull())
                .map(SlotResponse::from)
                .toList();
    }
}
