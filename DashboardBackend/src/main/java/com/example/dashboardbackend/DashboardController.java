package com.example.dashboardbackend;

import com.example.dashboardbackend.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardRepository repository;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.ingest.secret}")
    private String apiSecret;


    @GetMapping("/trends")
    public List<Map<String, Object>> getTrend(@RequestParam(defaultValue = "Daily") String dateRange) {
        return dashboardService.getTrendData(dateRange);
    }

    @GetMapping("/tickets")
    public List<Transaction> getTicket(@RequestParam(defaultValue = "Daily") String dateRange, @RequestParam(defaultValue = "amount") String sortBy) {
        return dashboardService.getTopTickets(dateRange, sortBy);
    }

    @GetMapping("/merchants")
    public List<Map<String, Object>> getMerchants(@RequestParam(defaultValue = "Daily") String dateRange, @RequestParam(defaultValue="amount") String sortBy) {
        return dashboardService.getTopMerchants(dateRange, sortBy);
    }

    @GetMapping("/payments")
    public List<Map<String, Object>> getPayments(@RequestParam(defaultValue = "Daily") String dateRange, @RequestParam(defaultValue = "volume") String sortBy) {
        return dashboardService.getTopPaymentMethods(dateRange, sortBy);
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestTransaction(
            @RequestBody Transaction t,
            @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        try {
            // Convert Object back to JSON
            String jsonPayload = objectMapper.writeValueAsString(t);

            // --- DEBUG LOGS (Look at these in your console!) ---
            System.out.println("========================================");
            System.out.println("1. Java Rebuilt JSON: " + jsonPayload);
            System.out.println("2. Client Signature:  " + signature);

            String serverSignature = securityUtil.calculateHMAC(jsonPayload, apiSecret);
            System.out.println("3. Server Signature:  " + serverSignature);
            System.out.println("========================================");
            // ----------------------------------------------------

            if (!securityUtil.isValidSignature(jsonPayload, signature, apiSecret)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Signature");
            }

            if (t.getTimestamp() == null) t.setTimestamp(LocalDateTime.now());
            repository.save(t);
            return ResponseEntity.ok("Transaction Saved Successfully");

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Json Format");
        }
    }



}