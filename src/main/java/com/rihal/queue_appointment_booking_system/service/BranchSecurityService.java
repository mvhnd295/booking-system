package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.domain.entity.Staff;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.RoleName;
import com.rihal.queue_appointment_booking_system.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// Service acts as a helper service to perform authorization on branch-scoped actions
@Service
@RequiredArgsConstructor
public class BranchSecurityService {

    private StaffRepository staffRepository;

    /**
     * Returns the branch ID the actor is allowed to operate on.
     *
     * - ADMIN → returns null (no restriction, null means "all branches")
     * - MANAGER → returns their own branch ID; throws 403 if they try to act on a different branch
     *
     * @param actor          the authenticated user
     * @param requestedBranchId the branch being targeted (null = "all" for list operations)
     * @return the branch ID to filter by, or null for no filter (admin)
     */
    public UUID resolveAllowedBranchId(User actor, UUID requestedBranchId) {
        if (actor.getRole().getName() == RoleName.ADMIN) {
            return requestedBranchId; // admin may pass a specific branch or null for all
        }

        // Manager — load their branch
        UUID managerBranchId = getManagerBranchId(actor);

        // If they specified a branch, it must be their own
        if (requestedBranchId != null && !requestedBranchId.equals(managerBranchId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only manage resources within your own branch.");
        }

        return managerBranchId;
    }

    /**
     * Asserts that the actor is allowed to act on the given branch.
     * Throws 403 if a manager tries to act on a different branch.
     */
    public void assertBranchAccess(User actor, UUID targetBranchId) {
        if (actor.getRole().getName() == RoleName.ADMIN) return;

        UUID managerBranchId = getManagerBranchId(actor);
        if (!managerBranchId.equals(targetBranchId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only manage resources within your own branch.");
        }
    }

    /**
     * Returns the branch ID for the given manager/staff actor.
     */
    public UUID getManagerBranchId(User actor) {
        Staff staff = staffRepository.findById(actor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Staff profile not found for actor."));
        return staff.getBranch().getId();
    }
}
