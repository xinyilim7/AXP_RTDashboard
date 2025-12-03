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
@CrossOrigin(origins = "*") // Allow external sources for testing
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardRepository repository; // Links to Database

    @Autowired
    private SecurityUtil securityUtil;      // Links to your Security Logic

    @Autowired
    private ObjectMapper objectMapper;      // Tool to convert Object -> JSON String

    @Value("${app.ingest.secret}")          // Reads 'app.ingest.secret' from application.properties
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
    public ResponseEntity<String> ingestTransaction(@RequestBody Transaction t, @RequestHeader(value = "X-Signature", required = false) String signature) {
        try{
            // 1. Reconstruct JSON String from Object (Needed for signature check)
            String jsonPayload=objectMapper.writeValueAsString(t);

            // 2. Verify Signature
            if(!securityUtil.isValidSignature(jsonPayload, signature, apiSecret)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Signature");
            }

            // 3. Add Timestamp If Missing
            if(t.getTimestamp() == null){
                t.setTimestamp(LocalDateTime.now());
            }

            // 4. Save directly to DB
            repository.save(t);
            return ResponseEntity.ok("Transaction Saved Successfully");
        }catch(JsonProcessingException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Json Format");
        }
    }



}