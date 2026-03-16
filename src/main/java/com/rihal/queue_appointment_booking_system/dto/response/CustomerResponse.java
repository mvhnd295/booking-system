package com.rihal.queue_appointment_booking_system.dto.response;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        String phone,
        boolean active,
        String imageUrl // /api/files/id-image/{id} null if no image on file
) {
}
