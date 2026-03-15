package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.dto.request.LoginRequest;
import com.rihal.queue_appointment_booking_system.dto.request.RegisterRequest;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.AuthResponse;
import com.rihal.queue_appointment_booking_system.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * /api/auth/register
     * Content-Type: multipart/form-data
     * @param username
     * @param email
     * @param password
     * @param fullname
     * @param phone
     * @param idImage file
     * @return 201 CREATED
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestParam @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,

            @RequestParam @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @RequestParam @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            @RequestParam @NotBlank(message = "Full name is required")
            String fullname,

            // Optional at this stage — will be required once file upload is wired
            @RequestParam String phone,

            // ID Image
            @RequestPart("idImage") MultipartFile idImage
    ) {

        AuthResponse response = authService.register(
                username,
                email,
                password,
                fullname,
                phone,
                idImage
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * /api/auth/login
     * Content-Type: application/json
     * @param request
     * @return 200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
