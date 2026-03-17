package com.rihal.queue_appointment_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNoteRequest (
        @NotBlank(message = "Note cannot be blank")
        @Size(max = 500, message = "Note cannot exceed 500 characters")
        String note
){
}
