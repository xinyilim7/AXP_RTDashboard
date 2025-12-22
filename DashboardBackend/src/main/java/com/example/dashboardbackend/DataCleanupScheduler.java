package com.example.dashboardbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DataCleanupScheduler{

    private static final Logger logger = LoggerFactory.getLogger(DataCleanupScheduler.class);

    @Autowired
    private DashboardRepository repository;

    @Scheduled(cron = "0 0 3 * * ?") // Run at 03:00:00 AM every day
    @Transactional
    public void deleteOldTransactions(){
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(2);
        String cutoffString = cutoffDate.toString();
        logger.info(">>> SCHEDULER: Deleting Transaction Before {}", cutoffString);

        try{
            repository.deleteByTimestampBefore(cutoffString);
            logger.info(">>> SCHEDULER: Cleanup Completed");
        } catch (java.lang.Exception e) {
            logger.error(">>> SCHEDULER ERROR: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}