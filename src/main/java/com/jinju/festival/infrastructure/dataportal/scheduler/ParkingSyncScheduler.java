package com.jinju.festival.infrastructure.dataportal.scheduler;

import com.jinju.festival.infrastructure.dataportal.service.DataPortalParkingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "api.data-portal.sync-enabled", havingValue = "true", matchIfMissing = true)
public class ParkingSyncScheduler {

    private final DataPortalParkingSyncService parkingSyncService;

    @Scheduled(fixedRateString = "${api.data-portal.sync-fixed-rate:300000}", initialDelay = 15000)
    public void scheduleParkingStatusSync() {
        log.info("Executing scheduled real-time parking status synchronization...");
        try {
            parkingSyncService.syncParkingStatus();
        } catch (Exception e) {
            log.error("Error occurred during scheduled parking sync: {}", e.getMessage(), e);
        }
    }
}
