package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.Attachment;
import com.rihal.queue_appointment_booking_system.domain.entity.Customer;
import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.domain.enums.RoleName;
import com.rihal.queue_appointment_booking_system.repository.AppointmentRepository;
import com.rihal.queue_appointment_booking_system.repository.AttachmentRepository;
import com.rihal.queue_appointment_booking_system.repository.CustomerRepository;
import com.rihal.queue_appointment_booking_system.storage.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Tag(name = "Files", description = "Secured file retrieval for ID images and attachments")
public class FileController {

    private final FileStorageService fileStorageService;
    private final CustomerRepository customerRepository;
    private final AttachmentRepository attachmentRepository;
    private final AppointmentRepository appointmentRepository;

    // ── ID Image ──────────────────────────────────────────────────────────────
    // Access: ADMIN only
    /**
     * GET /api/files/id-image/{customerId}
     */
    @GetMapping("/id-image/{customerId}")
    public ResponseEntity<Resource> getIdImage(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID customerId
    ) {
        // Only ADMIN allowed
        RoleName role = actor.getRole().getName();
        if (role != RoleName.ADMIN) {
            throw new SecurityException("Access denied.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        if (customer.getIdImagePath() == null) {
            throw new IllegalArgumentException("This customer has no ID image on file.");
        }

        return serveFile(customer.getIdImagePath(), customer.getIdImageType());
    }

    // ── Appointment Attachment ────────────────────────────────────────────────
    // Access: owner customer, STAFF, BRANCH_MANAGER, ADMIN

    /**
     * GET /api/files/attachments/{attachmentId}
     */
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<Resource> getAttachment(
            @AuthenticationPrincipal User actor,
            @PathVariable UUID attachmentId
    ) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));

        RoleName role = actor.getRole().getName();

        // Customers can only access their own appointment's attachment
        if (role == RoleName.CUSTOMER) {
            boolean ownsAttachment = appointmentRepository
                    .findByCustomerIdOrderByCreatedAtDesc(actor.getId())
                    .stream()
                    .anyMatch(a -> a.getAttachment() != null &&
                            a.getAttachment().getId().equals(attachmentId));

            if (!ownsAttachment) {
                throw new SecurityException("Access denied.");
            }
        }
        // STAFF, BRANCH_MANAGER, ADMIN — allowed through without further check

        return serveFile(attachment.getFilePath(), attachment.getMimeType());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<Resource> serveFile(String relativePath, String mimeType) {
        try {
            Path filePath = fileStorageService.load(relativePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File is not readable: " + relativePath);
            }

            MediaType mediaType;
            try {
                mediaType = mimeType != null
                        ? MediaType.parseMediaType(mimeType)
                        : MediaType.APPLICATION_OCTET_STREAM;
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Failed to serve file: " + e.getMessage(), e);
        }
    }
}
