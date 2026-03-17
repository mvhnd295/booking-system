package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.audit.AuditService;
import com.rihal.queue_appointment_booking_system.domain.entity.*;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.dto.request.SlotRequest;
import com.rihal.queue_appointment_booking_system.dto.request.UpdateSlotRequest;
import com.rihal.queue_appointment_booking_system.dto.response.SlotManagementResponse;
import com.rihal.queue_appointment_booking_system.dto.response.SlotMapper;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.repository.BranchRepository;
import com.rihal.queue_appointment_booking_system.repository.ServiceTypeRepository;
import com.rihal.queue_appointment_booking_system.repository.SlotRepository;
import com.rihal.queue_appointment_booking_system.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotManagementService {

    private final SlotRepository slotRepository;
    private final ServiceTypeRepository serviceRepo;
    private final StaffRepository staffRepo;
    private final BranchRepository branchRepo;
    private final BranchSecurityService branchSecurityService;
    private final AuditService auditService;

    // Create a slot returns a list of slots
    // can work for bulk or single slot creation (list containing one slot)
    @CacheEvict(value = "slots", allEntries = true)
    @Transactional
    public List<SlotManagementResponse> createSlots(User actor, List<SlotRequest> requests) {
        return requests.stream().map(r -> {
            // Check branch access
            UUID branchId = r.branchId();
            branchSecurityService.assertBranchAccess(actor, branchId);
            Branch branch = branchRepo.findById(branchId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Branch not found: " + branchId));

            // Get the service type of the slot
            UUID serviceId = r.serviceTypeId();
            ServiceType serviceType = serviceRepo.findById(serviceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Service type not found in this branch: " + serviceId));

            // Staff might not be assigned to a slot so nullable
            Staff staff = null;
            if (r.staffId() != null) {
                staff = staffRepo.findById(r.staffId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Staff not found: " + r.staffId()));
                // Check staff's branch matches request branch
                if (!staff.getBranch().getId().equals(branch.getId())) {
                    throw new IllegalStateException("Staff member does not belong to this branch");
                }
            }

            // Make sure slot time make sense (end time after start time)
            LocalDateTime startAt = r.startAt();
            LocalDateTime endAt = r.endAt();
            log.debug("createSlots: slot start and end time: {} - {}", startAt, endAt);
            validateSlotTimes(startAt, endAt);

            // Create the slot from the request and save
            Slot slot = new Slot();
            slot.setBranch(branch);
            slot.setServiceType(serviceType);
            slot.setStaff(staff);
            slot.setStartAt(startAt);
            slot.setEndAt(endAt);
            slot.setBooked(0);
            slot.setCapacity(r.capacity());
            slot.setActive(r.active());
            slot.setDeleted(false);
            slotRepository.save(slot);

            auditService.log(
                    AuditAction.SLOT_CREATED,
                    actor,
                    EntityType.SLOT,
                    slot.getId(),
                    branch,
                    Map.of("serviceType", serviceType.getName(), "startAt", slot.getStartAt().toString())
            );
            return SlotMapper.toResponse(slot);
        }).toList();
    }

    // List slots (Admin - all, Manager - branch-scoped)
    @Cacheable(value = "slots", key = "#actor.id + '_' + #branchId + '_' + #term + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResponse<SlotManagementResponse> listSlots(User actor, UUID branchId, String term, Pageable pageable) {
        UUID allowedBranchId = branchSecurityService.resolveAllowedBranchId(actor, branchId);

        Page<Slot> page = (allowedBranchId == null)
                ? slotRepository.searchAll(term, pageable)
                : slotRepository.searchByBranch(term, allowedBranchId, pageable);

        List<SlotManagementResponse> mapped = page.getContent().stream().map(SlotMapper::toResponse).toList();
        return PagedResponse.from(page, mapped);
    }

    // Update a slot
    @Transactional
    public SlotManagementResponse updateSlot(
            User actor,
            UUID slotId,
            UpdateSlotRequest request
    ) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Slot not found: " + slotId));

        branchSecurityService.assertBranchAccess(actor, slot.getBranch().getId());

//        // Cant change the branch of a slot
//        if (!slot.getBranch().getId().equals(request.branchId())) {
//            throw new IllegalStateException("Cannot move a slot to a different branch.");
//        }

        // Get the service type of the slot
        if (request.serviceTypeId() != null) {
            ServiceType serviceType = serviceRepo
                    .findByIdAndBranchId(request.serviceTypeId(), slot.getBranch().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Service type not found in this branch."));
            slot.setServiceType(serviceType);
        }

        // if null — leave slot.getStaff() unchanged
        if (request.staffId() != null) {
            Staff staff = staffRepo.findById(request.staffId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Staff not found: " + request.staffId()));
            if (!staff.getBranch().getId().equals(slot.getBranch().getId())) {
                throw new IllegalArgumentException(
                        "Staff member does not belong to this branch.");
            }
            slot.setStaff(staff);
        }
        LocalDateTime startAt = request.startAt() != null ? request.startAt() : slot.getStartAt();
        LocalDateTime endAt = request.endAt() != null ? request.endAt() : slot.getEndAt();
        log.debug("updateSlot: slot start and end time: {} - {}", startAt, endAt);
        validateSlotTimes(startAt, endAt);

        if (request.capacity() != null && request.capacity() < slot.getBooked()) {
            throw new IllegalStateException(
                    "Cannot set capacity below current booked count (" + slot.getBooked() + ")."
            );
        }
        if (request.startAt() != null) slot.setStartAt(request.startAt());
        if (request.endAt() != null) slot.setEndAt(request.endAt());
        if (request.capacity() != null) slot.setCapacity(request.capacity());
        if (request.active() != null) slot.setActive(request.active());
        slotRepository.save(slot);

        auditService.log(
                AuditAction.SLOT_UPDATED,
                actor,
                EntityType.SLOT,
                slot.getId(),
                slot.getBranch()
        );

        return SlotMapper.toResponse(slot);
    }

    // Soft delete slots
    @Transactional
    public void softDeleteSlot(User actor, UUID slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Slot not found."));

        branchSecurityService.assertBranchAccess(actor, slot.getBranch().getId());

        if (slot.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Slot is already deleted.");
        }

        slot.setDeleted(true);
        slot.setDeletedAt(LocalDateTime.now());
        slot.setActive(false);
        slotRepository.save(slot);

        auditService.log(
                AuditAction.SLOT_SOFT_DELETED,
                actor,
                EntityType.SLOT,
                slot.getId(),
                slot.getBranch(),
                Map.of("deletedAt", slot.getDeletedAt().toString())
        );
    }

    // Private helper
    private void validateSlotTimes(LocalDateTime startAt, LocalDateTime endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException(
                    "End time must be after start time.");
        }
    }

}
