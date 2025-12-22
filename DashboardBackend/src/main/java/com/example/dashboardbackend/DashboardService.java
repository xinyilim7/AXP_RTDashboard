package com.example.dashboardbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.awt.geom.Arc2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    @Autowired
    private DashboardRepository repository; // Link to DB

    //==================================================================
    // PUBLIC METHOD (CALL BY CONTROLLER)
    //==================================================================
    // 1. Trend Data---
    public List<Map<String, Object>> getTrendData(String dateRange) {
        LocalDateTime start = getStartDate(dateRange);
        // Get raw data for trends to handle time bucket in java
        List<Transaction> filtered = repository.findByTimestampAfter(start.toString());

        if ("Daily".equalsIgnoreCase(dateRange)) {
            return aggregateHourlyTrend(filtered);
        } else if ("Weekly".equalsIgnoreCase(dateRange)) {
            return aggregateWeeklyTrend(filtered);
        } else {
            return aggregateMonthlyTrend(filtered);
        }
    }

    // 2. Ticket Data---
    public List<Transaction> getTopTickets(String dateRange, String sortBy) {
        LocalDateTime start = getStartDate(dateRange);
        if ("timestamp".equalsIgnoreCase(sortBy)) {
            return repository.findTop10ByTimestampAfterOrderByTimestampDesc(start.toString());
        } else {
            return repository.findTop10ByTimestampAfterOrderByAmountDesc(start.toString());
        }
    }

    // 3. Merchant Data---
    public List<Map<String, Object>> getTopMerchants(String dateRange, String sortBy) {
        LocalDateTime start = getStartDate(dateRange);
        List<Object[]> rawStats = repository.findMerchantStats(start.toString());
        return processAndSortStats(rawStats, sortBy, 10, "merchant");
    }

    public List<Map<String, Object>> getTopPaymentMethods(String dateRange, String sortBy) {
        LocalDateTime start = getStartDate(dateRange);
        List<Object[]> rawStats = repository.findPaymentMethodStats(start.toString());

        List<Map<String, Object>> results = rawStats.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();

            // 1. Get existing data
            String category = (String) row[0];
            double totalAmount = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            long totalVolume = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            long successCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            long failedCount = row[4] != null ? ((Number) row[4]).longValue() : 0L;

            map.put("category", category);
            map.put("amount", totalAmount);
            map.put("volume", totalVolume);
            map.put("success", successCount);
            map.put("failed", failedCount);

            if (totalVolume > 0) {
                double avgTicket = totalAmount / totalVolume;

                map.put("successAmount", avgTicket * successCount);
                map.put("failedAmount", avgTicket * failedCount);
            } else {
                map.put("successAmount", 0.0);
                map.put("failedAmount", 0.0);
            }

            return map;
        }).collect(Collectors.toList());

        // 3. Sorting Logic (Keep existing)
        Comparator<Map<String, Object>> comparator;
        if ("volume".equalsIgnoreCase(sortBy)) {
            comparator = (m1, m2) -> Long.compare(
                    ((Number) m2.get("volume")).longValue(),
                    ((Number) m1.get("volume")).longValue()
            );
        } else {
            comparator = (m1, m2) -> Double.compare(
                    ((Number) m2.get("amount")).doubleValue(),
                    ((Number) m1.get("amount")).doubleValue()
            );
        }

        return results.stream()
                .sorted(comparator)
                .limit(5)
                .collect(Collectors.toList());
    }

    //==================================================================
    // HELPER METHOD
    //==================================================================
    /*Date Filtering*/
    private LocalDateTime getStartDate(String dateRange) {
        LocalDateTime now = LocalDateTime.now();
        if ("Daily".equalsIgnoreCase(dateRange)) {
            return now.withHour(0).withMinute(0).withSecond(0);
        } else if ("Weekly".equalsIgnoreCase(dateRange)) {
            return now.minusDays(7).withHour(0).withMinute(0).withSecond(0);
        } else {
            return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
    }

    /*Daily Trend*/
    private List<Map<String, Object>> aggregateHourlyTrend(List<Transaction> events) {
        // 1. Debugging: Check if data is actually arriving here
        System.out.println("AggregateHourlyTrend received " + events.size() + " transactions.");
        // 2. Create 1440 buckets (00:00 to 23:59)
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            String hStr = String.format("%02d", h);
            for (int m = 0; m < 60; m++) {
                Map<String, Object> bucket = new HashMap<>();
                bucket.put("minute", String.format("%s:%02d", hStr, m));
                bucket.put("success", 0);
                bucket.put("failed", 0);
                bucket.put("pending", 0);
                bucket.put("total", 0);
                bucket.put("amount", 0.0);
                buckets.add(bucket);
            }
        }
        // 3. Fill buckets
        for (Transaction t : events) {
            LocalDateTime txTime = LocalDateTime.parse(t.getTimestamp());
            int index = txTime.getHour() * 60 + txTime.getMinute();
            if (index >= 0 && index < buckets.size()) {
                Map<String, Object> bucket = buckets.get(index);

                // Convert status to lowercase to match your map keys ("success", "failed")
                String status = t.getStatus().toLowerCase();

                if (bucket.containsKey(status)) {
                    bucket.put(status, (int) bucket.get(status) + 1);
                }

                double currentAmount = ((Number) bucket.get("amount")).doubleValue();
                bucket.put("amount", currentAmount + t.getAmount());
                bucket.put("total", (int) bucket.get("total") + 1);
            }
        }
        return buckets;
    }

    /*Weekly Trend*/
    private List<Map<String, Object>> aggregateWeeklyTrend(List<Transaction> events) {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        List<Map<String, Object>> buckets = new ArrayList<>();

        for (String day : days) {
            Map<String, Object> bucket = new HashMap<>();
            bucket.put("label", day);
            bucket.put("success", 0);
            bucket.put("failed", 0);
            bucket.put("pending", 0);
            bucket.put("total", 0);
            bucket.put("amount", 0.0);
            buckets.add(bucket);
        }

        for (Transaction t : events) {
            LocalDateTime txTime = LocalDateTime.parse(t.getTimestamp());

            int index = txTime.getDayOfWeek().getValue() - 1;
            Map<String, Object> bucket = buckets.get(index);

            String status = t.getStatus();
            bucket.put(status, (int) bucket.get(status) + 1);

            double currentAmount = ((Number) bucket.get("amount")).doubleValue();
            bucket.put("amount", currentAmount + t.getAmount());
            bucket.put("total", (int) bucket.get("total") + 1);
        }
        return buckets;
    }

    /*Monthly Trend*/
    private List<Map<String, Object>> aggregateMonthlyTrend(List<Transaction> events) {
        // 1. Get current date info
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue(); // 1-12
        int daysInMonth = now.lengthOfMonth(); // e.g., 30 for Nov

        // 2. Create buckets (One for each day of the month)
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            Map<String, Object> bucket = new HashMap<>();
            // Create label "01", "02", etc.
            bucket.put("label", (i < 10 ? "0" + i : String.valueOf(i)));
            bucket.put("success", 0);
            bucket.put("failed", 0);
            bucket.put("pending", 0);
            bucket.put("total", 0);
            bucket.put("amount", 0.0);
            buckets.add(bucket);
        }

        // 3. Loop through events
        for (Transaction t : events) {
            // Only process events in the current month/year
            LocalDateTime timestamp = LocalDateTime.parse(t.getTimestamp());
            if (timestamp.getYear() == year && timestamp.getMonthValue() == month) {

                int day = timestamp.getDayOfMonth(); // 1-31
                int bucketIndex = day - 1; // 0-30

                // Safety Check: Ensure index is valid
                if (bucketIndex >= 0 && bucketIndex < buckets.size()) {
                    Map<String, Object> bucket = buckets.get(bucketIndex);

                    if ("success".equalsIgnoreCase(t.getStatus())) {
                        bucket.put("success", (int) bucket.get("success") + 1);
                    } else if ("failed".equalsIgnoreCase(t.getStatus())) {
                        bucket.put("failed", (int) bucket.get("failed") + 1);
                    }
                    double currentAmount = ((Number) bucket.get("amount")).doubleValue();
                    bucket.put("amount", currentAmount + t.getAmount());
                    bucket.put("total", (int) bucket.get("total") + 1);
                }
            }
        }
        return buckets;
    }

    /*Generic helper to convert SQL results to Map and Sort*/
    private List<Map<String, Object>> processAndSortStats(List<Object[]> rawStats, String sortBy, int limit, String keyName) {
        List<Map<String, Object>> results = rawStats.stream().map(row ->{
            Map<String, Object> map = new HashMap<>();
            map.put("id", row[0]);
            map.put(keyName, row[0]);
            map.put("amount", row[1]);
            map.put("volume", row[2]);

            return map;
        }).collect(Collectors.toList());

        Comparator<Map<String,Object>> comparator;
        if ("volume".equalsIgnoreCase(sortBy)) {
            comparator=((m1, m2) -> Long.compare((Long) m2.get("volume"), (Long) m1.get("volume")));
        }else{
            comparator=((m1, m2) -> Double.compare((Double) m2.get("amount"), (Double) m1.get("amount")));
        }
        return results.stream().sorted(comparator).limit(limit).collect(Collectors.toList());
    }
}



