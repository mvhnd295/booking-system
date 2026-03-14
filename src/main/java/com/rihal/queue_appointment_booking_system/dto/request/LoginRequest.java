package com.rihal.queue_appointment_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        // Accepts username or email — handled in UserDetailsServiceImpl
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        String password
) {}
