```mermaid
sequenceDiagram
    participant A as AXPGW (Main System)
    participant S as Spring Boot Microservice (TV-Dashboard API)
    participant D as MySQL Database
    participant F as ReactJS Frontend (TV Dashboard)
    participant B as Browser/Display

    %% Data Flow (Write/Update)
    A->>S: 1. POST /api/transactions (Payment Captured - Success/Failed)
    activate S
    S->>D: 2. INSERT/UPDATE Transaction Data
    D-->>S: 3. Confirmation
    deactivate S

    %% Dashboard Data Polling (Read)
    loop Data Polling (e.g., every 5 seconds)
        B->>F: 4. Display Refreshes
        F->>S: 5. GET /api/dashboard/stats (Request latest aggregated data)
        activate S
        S->>D: 6. SELECT/Aggregate Data (e.g., success count, failure count)
        D-->>S: 7. Aggregated Statistics
        S-->>F: 8. JSON Response (Stats/Chart Data)
        deactivate S
        F->>B: 9. Render Updated Charts/Dashboard
    end
   ```


```mermaid
sequenceDiagram
    participant PS as Payment Services<br/>(FPX, Boost, etc.)
    participant PT as PostSalesTransactionTrigger<br/>(Post-Transaction Job)
    participant EP as ApplicationEventPublisher<br/>(Spring Events)
    participant EH as TransactionWebhookEventHandler<br/>(@EventListener)
    participant WS as TvDashboardWebhookService<br/>(@Async + @Retryable)
    participant TV as TV-Dashboard API<br/>(External Service)

    Note over PS: Payment response received<br/>Status updated in DB

    PS->>PT: execute(salesTransaction,<br/>statusId, ...)
    activate PT
    Note over PT: Process transaction logs,<br/>merchant callbacks, etc.

    alt Status is PAID (11) or FAILED (22)
        PT->>PT: Handle PAID/FAILED logic
    end

    PT->>EP: publishEvent(TransactionWebhookEvent)
    Note over EP: Event published to Spring context
    PT-->>PS: Continue normal flow<br/>(non-blocking)
    deactivate PT

    Note over EP,EH: Event dispatched asynchronously
    EP->>EH: handleTransactionWebhookEvent(event)
    activate EH
    Note over EH: Runs on taskExecutor thread pool

    EH->>WS: sendWebhook(event)
    activate WS
    Note over WS: Async method on separate thread

    WS->>WS: Check if webhook enabled<br/>(tvdashboard.webhook.enabled)

    alt Webhook Enabled
        WS->>WS: buildPayload(event)<br/>Extract: transactionId, merchantId,<br/>status, amount, paymentMethod

        WS->>TV: POST /api/transactions<br/>{payload as JSON}

        alt Success (2xx Response)
            TV-->>WS: 200 OK
            WS->>WS: log.info("Webhook sent successfully")
        else Non-2xx Response
            TV-->>WS: 4xx/5xx Error
            WS->>WS: log.warn("Non-success status")
        else Network/Connection Error
            TV--xWS: RestClientException
            WS->>WS: log.error("Failed to send")
            Note over WS: Exception thrown → triggers @Retryable
            Note over WS: Retry attempts: 3<br/>Delays: 1s, 2s, 4s (exponential backoff)
            WS->>TV: Retry POST /api/transactions
        end
    else Webhook Disabled
        WS->>WS: log.debug("Webhook disabled")
    end

    deactivate WS
    EH->>EH: Catch any exception<br/>(don't rethrow)
    deactivate EH

    Note over ST,EH: Main transaction flow unaffected<br/>Webhook processing is decoupled
```

