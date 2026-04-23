package com.clinicflow.clinic_flow.demo;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DemoScheduler {

    private final DemoResetService demoResetService;

    // Fires at 12:05 AM every day, Pacific time (handles PST/PDT automatically)
    @Scheduled(cron = "0 5 0 * * *", zone = "America/Los_Angeles")
    public void runDailyDemoReset() {
        log.info("DemoScheduler: starting daily demo reset");
        demoResetService.resetDemoData();
        log.info("DemoScheduler: daily demo reset finished");
    }
}
