package com.example.dashboardbackend;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;

@Entity
@Table(name="dashboard_transaction")
@Data
@JsonPropertyOrder({"id", "amount", "merchant", "status", "category", "timestamp"})
public class Transaction {

    @Id
    private String id;
    private Double amount;
    private String status;
    private String merchant;
    private String category;
    private String timestamp;

}
