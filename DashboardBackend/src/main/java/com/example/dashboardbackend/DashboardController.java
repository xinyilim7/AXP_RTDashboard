package com.example.dashboardbackend;

import com.example.dashboardbackend.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
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
            @RequestBody String rawJsonPayload, // 1. Receive the exact text the client sent
            @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        try {
            // --- DEBUG LOGS ---
            System.out.println("========================================");
            System.out.println("1. Raw Received JSON: " + rawJsonPayload); // Log the RAW string
            System.out.println("2. Client Signature:  " + signature);

            // 2. Validate using the RAW string immediately
            // We do NOT rebuild the JSON here. We use exactly what arrived.
            if (!securityUtil.isValidSignature(rawJsonPayload, signature, apiSecret)) {

                // Calculate what it SHOULD be for debugging purposes
                String expected = securityUtil.calculateHMAC(rawJsonPayload, apiSecret);
                System.out.println("3. Server Calculated: " + expected); // Mismatch debug
                System.out.println("========================================");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Signature");
            }

            System.out.println(">>> Signature MATCHED! Saving to DB...");
            System.out.println("========================================");

            // 3. NOW convert the string to the Object (Deserialization)
            Transaction t = objectMapper.readValue(rawJsonPayload, Transaction.class);

            // 4. Handle timestamp if missing (Safety check)
            if (t.getTimestamp() == null) {
                t.setTimestamp(LocalDateTime.now().toString());
            }

            // 5. Save to Database
            repository.save(t);
            return ResponseEntity.ok("Transaction Saved Successfully");

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Json Format");
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing transaction");
        }
    }
}