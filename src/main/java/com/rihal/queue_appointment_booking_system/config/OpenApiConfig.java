package com.rihal.queue_appointment_booking_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowCare Queue & Appointment Booking API")
                        .description("""
                                REST API for managing queues and appointments across FlowCare branches.

                                ## Authentication
                                Most endpoints require a JWT Bearer token. Obtain one via `POST /api/auth/login`.
                                Click **Authorize** and enter: `Bearer <your_token>`

                                ## Roles
                                - **ADMIN** — full system access
                                - **BRANCH_MANAGER** — branch-scoped management
                                - **STAFF** — view and update assigned appointments
                                - **CUSTOMER** — self-service booking and appointment management

                                ## Default Credentials (seed data)
                                | Role | Username | Password |
                                |------|----------|----------|
                                | Admin | admin | Admin@123 |
                                | Manager (Muscat) | mgr_muscat | Manager@123 |
                                | Manager (Suhar) | mgr_suhar | Manager@123 |
                                | Staff | staff_muscat_1 | Staff@123 |
                                | Customer | cust_ahmed | Customer@123 |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FlowCare")
                                .email("support@flowcare.om")))

                .servers(List.of(
                        new Server().url(baseUrl).description("Current server")))

                // JWT Bearer token security scheme
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")))

                // Apply globally — all endpoints require auth unless marked
                // @SecurityRequirements({})
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}