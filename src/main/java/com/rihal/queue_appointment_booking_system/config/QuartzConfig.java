package com.rihal.queue_appointment_booking_system.config;

import com.rihal.queue_appointment_booking_system.scheduler.SlotCleanupJob;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.TimeZone;

@Configuration
public class QuartzConfig {
    private static final String JOB_NAME    = "slotCleanupJob";
    private static final String JOB_GROUP   = "cleanupGroup";
    private static final String TRIGGER_NAME = "slotCleanupTrigger";

    @Value("${app.cleanup.cron}")
    private String cleanupCron;
    @Value("${app.cleanup.timezone}")
    private String cleanupTimezone;

    @Bean
    public JobDetail slotCleanupJobDetail() {
        return JobBuilder.newJob(SlotCleanupJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP)
                .withDescription("Hard deletes soft-deleted slots past the retention period")
                .storeDurably()  // keep job even if no triggers are associated
                .build();
    }

    @Bean
    public Trigger slotCleanupTrigger(JobDetail slotCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(slotCleanupJobDetail)
                .withIdentity(TRIGGER_NAME, JOB_GROUP)
                .withDescription("Runs daily at: " + cleanupCron)
                .withSchedule(
                        CronScheduleBuilder
                                .cronSchedule(cleanupCron) // configurable, currently: 2:00am every day
                                .inTimeZone(TimeZone.getTimeZone(cleanupTimezone)) // Set as Asia/Muscat currently
                                .withMisfireHandlingInstructionDoNothing() // skip if missed, don't catch up
                )
                .build();
    }
}
