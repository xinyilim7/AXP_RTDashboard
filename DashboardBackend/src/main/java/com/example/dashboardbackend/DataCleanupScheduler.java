package com.example.dashboardbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class DataCleanupScheduler{
    @Autowired
    private DashboardRepository repository;

    @Scheduled(cron = "0 0 3 * * ?") // Run at 03:00:00 AM every day
    @Transactional
    public void deleteOldTransactions(){
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(2);
        String cutoffString = cutoffDate.toString();
        System.out.println(">>> SCHEDULER: Deleting Transaction Before "+cutoffString);

        try{
            repository.deleteByTimestampBefore(cutoffString);
            System.out.println(">>> SCHEDULER: Cleanup Completed");
        } catch (java.lang.Exception e) {
            System.err.println(">>> SCHEDULER ERROR: "+e.getMessage());
            e.printStackTrace();
        }
    }
}