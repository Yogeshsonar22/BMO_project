package com.bmo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String referenceNumber;

    @Column(nullable = false)
    private String fromAccountNumber;

    // For UPI: recipient UPI ID; For Bill: biller ID
    @Column(nullable = false)
    private String recipient;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    private String currency = "CAD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String description;

    // Bill-specific
    private String billerName;
    private String billNumber;

    // UPI-specific
    private String upiId;

    private String remarks;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public enum PaymentType {
        UPI, BILL_PAYMENT
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}
