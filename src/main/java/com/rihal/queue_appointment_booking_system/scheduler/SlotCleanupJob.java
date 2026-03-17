package com.rihal.queue_appointment_booking_system.scheduler;

import com.rihal.queue_appointment_booking_system.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotCleanupJob implements Job {
    private final AppConfigService appConfigService;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("SlotCleanupJob triggered — running hard delete cleanup...");
        try {
            // Pass null as actor — system-initiated, not a user action
            int deleted = appConfigService.runCleanUp(null);
            log.info("SlotCleanupJob completed — {} slot(s) hard deleted.", deleted);
        } catch (Exception e) {
            log.error("SlotCleanupJob failed: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
