package com.example.dashboardbackend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Transaction, String> {
    // 1. Trend Data: Just get data after a specific date
    List<Transaction> findByTimestampAfter(String timestamp);

    // 2. Top 10 Ticket (Sort by amount)
    List<Transaction>findTop10ByTimestampAfterOrderByAmountDesc(String timestamp);

    // 3. Top 10 Ticket (Sort by timestamp)
    List<Transaction>findTop10ByTimestampAfterOrderByTimestampDesc(String timestamp);

    // 4. Top 10 Merchant (Group by merchant name)
    // Return [ MerchantName, TotalAmount, TotalVolume]
    @Query("SELECT t.merchant, SUM(t.amount), COUNT(t) " +
            "FROM Transaction t " +
            "WHERE t.timestamp >= :start AND t.status = 'success' " +
            "GROUP BY t.merchant ")
    List<Object[]> findMerchantStats(@Param("start") String start);

    // 5. Top 5 Payment (Group by payment method)
    @Query("SELECT t.category, SUM(t.amount), COUNT(t), " +
            "SUM(CASE WHEN LOWER(t.status) = 'success' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN LOWER(t.status) = 'failed' THEN 1 ELSE 0 END) " +
            "FROM Transaction t " +
            "WHERE t.timestamp >= :start " +
            "GROUP BY t.category")
    List<Object[]> findPaymentMethodStats(@Param("start") String start);

    //"WHERE t.timestamp >= :start AND t.status='success' " +

    // 6. Delete Last 2 Months Data
    void deleteByTimestampBefore(String cutoffTimestamp);

}
