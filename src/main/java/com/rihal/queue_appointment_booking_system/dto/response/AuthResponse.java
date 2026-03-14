package com.rihal.queue_appointment_booking_system.dto.response;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String username,
        String email,
        String fullName,
        String role
) {}
