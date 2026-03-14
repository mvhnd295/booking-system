package com.rihal.queue_appointment_booking_system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Read Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. No token or wrong format — pass through as unauthenticated
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Strip "Bearer " prefix
        final String token = authHeader.substring(7);

        try {
            // 4. Extract username from token claims
            final String username = jwtService.extractUsername(token);

            // 5. Only proceed if we have a username and no auth is set yet for this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load full user from DB
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 7. Validate token signature + expiry against the loaded user
                if (jwtService.isTokenValid(token, userDetails)) {

                    // 8. Build authentication object and put it in SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                          // no credentials needed post-auth
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Invalid token (expired, tampered, malformed) — just continue as unauthenticated
            // Spring Security will return 401 for protected endpoints
            SecurityContextHolder.clearContext();
        }

        // 9. Always continue the filter chain
        filterChain.doFilter(request, response);
    }
}
