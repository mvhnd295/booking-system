package com.rihal.queue_appointment_booking_system.service;

// Service for Admin and Managers to manage staff
import com.rihal.queue_appointment_booking_system.audit.AuditService;
import com.rihal.queue_appointment_booking_system.domain.entity.ServiceType;
import com.rihal.queue_appointment_booking_system.domain.entity.Staff;
import com.rihal.queue_appointment_booking_system.domain.entity.StaffServiceType;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.dto.response.StaffMapper;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.dto.response.StaffResponse;
import com.rihal.queue_appointment_booking_system.repository.ServiceTypeRepository;
import com.rihal.queue_appointment_booking_system.repository.StaffRepository;
import com.rihal.queue_appointment_booking_system.repository.StaffServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffManagementService {

    private final StaffRepository staffRepo;
    private final ServiceTypeRepository serviceTypeRepo;
    private final StaffServiceTypeRepository staffServiceTypeRepo;
    private final BranchSecurityService branchSecurityService;
    private final AuditService auditService;

    // List staff for Admin or Branch manager
    @Cacheable(value = "staff", key = "#actor.id + '_' + #term + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResponse<StaffResponse> listStaff(User actor, String term, Pageable pageable) {
        // Two-query pattern: 1) fetch IDs with SQL-level pagination
        Page<UUID> idPage;

        switch (actor.getRole().getName()) {
            case ADMIN -> idPage = staffRepo.findAllIds(term, pageable);
            case BRANCH_MANAGER -> {
                UUID branchId = branchSecurityService.getManagerBranchId(actor);
                idPage = staffRepo.findIdsByBranch(term, branchId, pageable);
            }
            default -> throw new SecurityException("Access denied.");
        }

        if (idPage.isEmpty()) {
            return PagedResponse.from(idPage, List.of());
        }

        // 2) Fetch full entities with all associations eagerly loaded
        List<Staff> staffList = staffRepo.findAllWithAssociationsByIds(idPage.getContent());
        Map<UUID, Staff> byId = staffList.stream()
                .collect(Collectors.toMap(Staff::getId, Function.identity()));

        List<StaffResponse> mapped = idPage.getContent().stream()
                .map(byId::get)
                .map(StaffMapper::toStaffResponse)
                .toList();
        return PagedResponse.from(idPage, mapped);
    }

    // Assign service to staff member
    // Takes a list of service type IDs because you can assign a single staff multiple service types
    @CacheEvict(value = "staff", allEntries = true)
    @Transactional
    public StaffResponse assignService(User actor, UUID staffId, List<UUID> serviceTypeIds) {
        Staff staff = resolveStaff(staffId);
        branchSecurityService.assertBranchAccess(actor, staff.getBranch().getId());

        // Batch-fetch all requested service types in one query and index by ID.
        // Without this, the loop below would fire one findByIdAndBranchId per ID — O(N) SELECTs.
        Map<UUID, ServiceType> serviceMap = serviceTypeRepo
                .findAllByIdInAndBranchId(serviceTypeIds, staff.getBranch().getId())
                .stream()
                .collect(Collectors.toMap(ServiceType::getId, Function.identity()));

        // Batch-check which service types are already assigned in one query — avoids N EXISTS queries.
        Set<UUID> alreadyAssigned = new HashSet<>(
                staffServiceTypeRepo.findAssignedServiceTypeIds(staff.getId(), serviceTypeIds));

        List<StaffServiceType> toSave = new ArrayList<>();
        for (UUID id : serviceTypeIds) {
            ServiceType service = Optional.ofNullable(serviceMap.get(id))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Service type does not belong to the same branch as this staff member: " + id));
            if (alreadyAssigned.contains(id)) continue;
            StaffServiceType sst = new StaffServiceType();
            sst.setStaff(staff);
            sst.setServiceType(service);
            toSave.add(sst);
        }

        if (!toSave.isEmpty()) {
            staffServiceTypeRepo.saveAll(toSave);
            for (StaffServiceType sst : toSave) {
                auditService.log(
                        AuditAction.STAFF_ASSIGNED_TO_SERVICE,
                        actor,
                        EntityType.STAFF,
                        staff.getId(),
                        staff.getBranch(),
                        Map.of("serviceTypeId", sst.getServiceType().getId().toString(),
                                "serviceTypeName", sst.getServiceType().getName())
                );
            }
        }

        // Reload with associations so the mapper can access staffServiceTypes without lazy loads
        return StaffMapper.toStaffResponse(resolveStaff(staffId));
    }

    // Remove/unassign a service from staff member
    public StaffResponse removeService(User actor, UUID staffId, UUID serviceTypeId) {
        Staff staff = resolveStaff(staffId);
        branchSecurityService.assertBranchAccess(actor, staff.getBranch().getId());
        if (!staffServiceTypeRepo.existsByStaffIdAndServiceTypeId(
                staff.getId(), serviceTypeId
        )) {
            throw new IllegalArgumentException(
                    "This staff member is not assigned to this service type: "
                    + serviceTypeId
            );
        }
        staffServiceTypeRepo.deleteByStaffIdAndServiceTypeId(staff.getId(), serviceTypeId);
        auditService.log(
                AuditAction.STAFF_REMOVED_FROM_SERVICE,
                actor,
                EntityType.STAFF,
                staff.getId(),
                staff.getBranch(),
                Map.of("ServiceTypeId", serviceTypeId.toString())
        );
        return StaffMapper.toStaffResponse(resolveStaff(staffId));
    }

    // helper — always loads with associations so StaffMapper never triggers lazy loads
    private Staff resolveStaff(UUID staffId) {
        return staffRepo.findByIdWithAssociations(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Staff not found: "+ staffId));
    }
}
