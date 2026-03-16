package com.rihal.queue_appointment_booking_system.dto.response;

import com.rihal.queue_appointment_booking_system.domain.entity.Appointment;

import java.util.ArrayList;

public class AppointmentMapper {
    private AppointmentMapper() {}

    public static AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getStatus(),
                a.getCreatedAt(),
                a.getUpdatedAt(),
                // Slot may be null after hard-delete
                a.getSlot() != null ? a.getSlot().getId() : null,
                a.getSlot() != null ? a.getSlot().getStartAt() : null,
                a.getSlot() != null ? a.getSlot().getEndAt() : null,

                // Denormalized fields to be always present (historical accuracy)
                a.getBranch().getId(), // Branch is a business requirement for every appointment
                a.getBranch().getName(),
                a.getServiceType().getId(), // Service Type is a business requirement for every appointment
                a.getServiceType().getName(),
                // Staff might leave -> deleted
                // OR
                // Appointment with no staff assigned to it
                // (e.g.: "Any available staff" appointment)
                a.getStaff() != null ? a.getStaff().getId() : null,
                a.getStaff() != null ? a.getStaff().getFullName() : null,

                // Attachment - nullable
                a.getAttachment() != null ? a.getAttachment().getId() : null,
                a.getAttachment() != null
                        ? "/api/files/attachments/" + a.getAttachment().getId()
                        : null,
                a.getAttachment() != null ? a.getAttachment().getOriginalName() : null,
                a.getAttachment() != null ? a.getAttachment().getMimeType() : null,
                a.getAttachment() != null ? a.getAttachment().getSizeBytes() : null
        );
    }
    public static StaffAppointmentResponse toStaffResponse(Appointment a) {
        return new StaffAppointmentResponse(
                a.getId(),
                a.getStatus(),
                a.getCreatedAt(),
                a.getUpdatedAt(),
                // Slot may be null after hard-delete
                a.getSlot() != null ? a.getSlot().getId() : null,
                a.getSlot() != null ? a.getSlot().getStartAt() : null,
                a.getSlot() != null ? a.getSlot().getEndAt() : null,

                // Denormalized fields to be always present (historical accuracy)
                a.getBranch().getId(), // Branch is a business requirement for every appointment
                a.getBranch().getName(),
                a.getServiceType().getId(), // Service Type is a business requirement for every appointment
                a.getServiceType().getName(),
                // Staff might leave -> deleted
                // OR
                // Appointment with no staff assigned to it
                // (e.g.: "Any available staff" appointment)
                a.getStaff() != null ? a.getStaff().getId() : null,
                a.getStaff() != null ? a.getStaff().getFullName() : null,

                // Attachment - nullable
                a.getAttachment() != null ? a.getAttachment().getId() : null,
                a.getAttachment() != null
                        ? "/api/files/attachments/" + a.getAttachment().getId()
                        : null,
                a.getAttachment() != null ? a.getAttachment().getOriginalName() : null,
                a.getAttachment() != null ? a.getAttachment().getMimeType() : null,
                a.getAttachment() != null ? a.getAttachment().getSizeBytes() : null,

                // Staff/Admin/Manager related fields (internal notes and customer contact info)
                a.getInternalNotes() != null ? a.getInternalNotes() : new ArrayList<>(),
                // Customer info
                a.getCustomer().getId(),
                a.getCustomer().getFullName(),
                a.getCustomer().getPhone()
        );
    }
}
