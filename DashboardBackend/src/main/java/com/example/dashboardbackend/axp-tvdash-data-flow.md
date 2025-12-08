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