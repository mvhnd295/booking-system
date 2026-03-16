package com.rihal.queue_appointment_booking_system.controller;

import com.rihal.queue_appointment_booking_system.domain.entity.AuditLog;
import com.rihal.queue_appointment_booking_system.dto.response.ApiResponse;
import com.rihal.queue_appointment_booking_system.dto.response.AuditLogResponse;
import com.rihal.queue_appointment_booking_system.service.AuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogQueryService auditService;

    // GET /api/audit-logs
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> list() {
        List<AuditLogResponse> logs = auditService.listLogs();
        return ResponseEntity.ok(ApiResponse.success("Audit Logs retrieved.", logs));
    }

    // GET /api/audit-logs/export
    // export logs to CSV
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() throws IOException {
        byte[] csvBytes = auditService.exportCsv();

        String filename = "audit-logs-" + LocalDateTime.now() + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(csvBytes.length);

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}
