package com.rihal.queue_appointment_booking_system.service;

import com.rihal.queue_appointment_booking_system.domain.entity.Customer;
import com.rihal.queue_appointment_booking_system.domain.entity.Role;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.RoleName;
import com.rihal.queue_appointment_booking_system.dto.request.LoginRequest;
import com.rihal.queue_appointment_booking_system.dto.request.RegisterRequest;
import com.rihal.queue_appointment_booking_system.dto.response.AuthResponse;
import com.rihal.queue_appointment_booking_system.repository.CustomerRepository;
import com.rihal.queue_appointment_booking_system.repository.RoleRepository;
import com.rihal.queue_appointment_booking_system.repository.UserRepository;
import com.rihal.queue_appointment_booking_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check for duplicates
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Fetch CUSTOMER role
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException(
                        "CUSTOMER role not found — ensure seed data is loaded"));

        // Build customer entity
        Customer customer = new Customer();
        customer.setUsername(request.username());
        customer.setEmail(request.email());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer.setFullName(request.fullName());
        customer.setPhone(request.phone());
        customer.setRole(customerRole);
        customer.setActive(true);

        // idImagePath is null for now — will be required once file upload is wired
        customer.setIdImagePath(null);

        customerRepository.save(customer);

        // Generate JWT
        String token = jwtService.generateToken(customer);

        return toAuthResponse(customer, token);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {

        // AuthenticationManager validates credentials — throws BadCredentialsException if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.usernameOrEmail(),
                        request.password()
                )
        );

        // Credentials are valid — load full user
        var user = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(user);

        return toAuthResponse(user, token);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse toAuthResponse(
            User user,
            String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName().name()
        );
    }
}
