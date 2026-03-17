package com.rihal.queue_appointment_booking_system.audit;

import com.opencsv.CSVWriter;
import com.rihal.queue_appointment_booking_system.domain.entity.AuditLog;
//import com.rihal.queue_appointment_booking_system.domain.entity.User;
import com.rihal.queue_appointment_booking_system.dto.response.AuditLogMapper;
import com.rihal.queue_appointment_booking_system.dto.response.AuditLogResponse;
import com.rihal.queue_appointment_booking_system.dto.response.PagedResponse;
import com.rihal.queue_appointment_booking_system.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogQueryService {

    private final AuditLogRepository auditRepo;
//    private final BranchSecurityService branchSecurityService;

    // List Audit Logs (Admin only)
    @Cacheable(value = "auditLogs", key = "#term + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PagedResponse<AuditLogResponse> listLogs(String term, Pageable pageable) {
        // Not sure if Branch managers can view logs too
        // if so pass User actor -> get ID and use a switch statement on it
        // then use branchSecurityService to get the branch ID of the manager
        Page<AuditLog> page = auditRepo.searchLogs(term, pageable);
        List<AuditLogResponse> mapped = page.getContent().stream().map(AuditLogMapper::toResponse).toList();
        return PagedResponse.from(page, mapped);
    }

    // Export to CSV
    @Transactional(readOnly = true)
    // Method return type byte[] -> raw file bytes instead of File
    // Because the API will stream files to the client
    // Browser downloads the file automatically
    public byte[] exportCsv() throws IOException {
        // Get all logs from newest to oldest
        List<AuditLog> logs = auditRepo.findAllByOrderByTimestampDesc();
        // Create memory output stream
        // Normal file -> in disk
        // ByteArrayOutputStream -> in RAM (virtual file in memory) will get converted to list of bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Using opencsv package class CSVWriter to write csv rows instead of writing it manually ourselves
        try (CSVWriter writer = new CSVWriter(
                // Converting characters into bytes
                // CSVWriter writes text (Characters) -> ByteArrayOutputStream stores bytes
                new OutputStreamWriter(out, StandardCharsets.UTF_8))) {

            // Header row (like the column names in Excel)
            writer.writeNext(new String[]{
                    "id", "action", "actor_id", "actor_role",
                    "target_entity_type", "target_entity_id",
                    "branch_id", "branch_name",
                    "metadata", "timestamp"
            });

            // Data rows
            for (AuditLog log : logs) {
                writer.writeNext(new String[]{
                        str(log.getId()),
                        str(log.getAction()),
                        str(log.getActorId()),
                        str(log.getActorRole()),
                        str(log.getTargetEntityType()),
                        log.getTargetEntityId(),
                        log.getBranch() != null ? log.getBranch().getId().toString() : "",
                        log.getBranch() != null ? log.getBranch().getName() : "",
                        log.getMetadata() != null ? log.getMetadata().toString() : "",
                        str(log.getTimestamp())
                });
            }
        }

        return out.toByteArray();
    }
    // private helper
    private String str(Object o) {
        return o != null ? o.toString() : "";
    }
}
