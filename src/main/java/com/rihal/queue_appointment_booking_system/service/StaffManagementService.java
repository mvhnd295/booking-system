package com.rihal.queue_appointment_booking_system.service;

// Service for Admin and Managers to manage staff
import com.rihal.queue_appointment_booking_system.domain.entity.ServiceType;
import com.rihal.queue_appointment_booking_system.domain.entity.Staff;
import com.rihal.queue_appointment_booking_system.domain.entity.StaffServiceType;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.AuditAction;
import com.rihal.queue_appointment_booking_system.domain.enums.EntityType;
import com.rihal.queue_appointment_booking_system.dto.response.StaffMapper;
import com.rihal.queue_appointment_booking_system.dto.response.StaffResponse;
import com.rihal.queue_appointment_booking_system.repository.ServiceTypeRepository;
import com.rihal.queue_appointment_booking_system.repository.StaffRepository;
import com.rihal.queue_appointment_booking_system.repository.StaffServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffManagementService {

    private final StaffRepository staffRepo;
    private final ServiceTypeRepository serviceTypeRepo;
    private final StaffServiceTypeRepository staffServiceTypeRepo;
    private final BranchSecurityService branchSecurityService;
    private final AuditService auditService;

    // List staff for Admin or Branch manager
    @Transactional(readOnly = true)
    public List<StaffResponse> listStaff(User actor) {
        List<Staff> staffList;

        switch (actor.getRole().getName()) {
            case ADMIN -> staffList = staffRepo.findAll();
            case BRANCH_MANAGER -> {
                UUID branchId = branchSecurityService.getManagerBranchId(actor);
                staffList = staffRepo.findAllByBranchId(branchId);
            }
            default -> throw new SecurityException("Access denied.");
        }

        return staffList.stream().map(StaffMapper::toStaffResponse).toList();
    }

    // Assign service to staff member
    // Takes a list of service type IDs because you can assign a single staff multiple service types
    @Transactional
    public StaffResponse assignService(User actor, UUID staffId, List<UUID> serviceTypeIds) {
        Staff staff = resolveStaff(staffId);

        // Make sure actor (admin, manager) can act on the staff's branch
        branchSecurityService.assertBranchAccess(actor, staff.getBranch().getId());

        for (UUID id : serviceTypeIds) {
            // Check service type belongs to the same branch as staff member
            ServiceType service = serviceTypeRepo
                    .findByIdAndBranchId(id, staff.getBranch().getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Service type does not belong to the same branch as this staff member: "
                                    + id
                    ));
            // Check if the staff member is already assigned the requested service type
            // Continue if so
            if (staffServiceTypeRepo.existsByStaffIdAndServiceTypeId(staff.getId(), id)) continue;

            // Assign and audit
            StaffServiceType sst = new StaffServiceType();
            sst.setStaff(staff);
            sst.setServiceType(service);
            staffServiceTypeRepo.save(sst);

            auditService.log(
                    AuditAction.STAFF_ASSIGNED_TO_SERVICE,
                    actor,
                    EntityType.STAFF,
                    staff.getId(),
                    staff.getBranch(),
                    Map.of("serviceTypeId", id.toString(),
                            "serviceTypeName", service.getName())
            );
        }
        // Reload the staff again to get updated services assigned
        return StaffMapper.toStaffResponse(resolveStaff(staffId));
    }

    // Remove/unassign a service from staff member
    public StaffResponse removeService(User actor, UUID staffId, UUID serviceTypeId) {
        Staff staff = resolveStaff(staffId);
        branchSecurityService.assertBranchAccess(actor, staff.getBranch().getId());
        if (staffServiceTypeRepo.existsByStaffIdAndServiceTypeId(
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

    // helper
    private Staff resolveStaff(UUID staffId) {
        return staffRepo.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Staff not found: "+ staffId));
    }
}
