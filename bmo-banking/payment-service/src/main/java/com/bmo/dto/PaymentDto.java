package com.bmo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpiRequest {
        @NotBlank(message = "Source account is required")
        private String fromAccountNumber;

        @NotBlank(message = "UPI ID is required")
        private String upiId;

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillPaymentRequest {
        @NotBlank(message = "Source account is required")
        private String fromAccountNumber;

        @NotBlank(message = "Biller name is required")
        private String billerName;

        @NotBlank(message = "Bill number is required")
        private String billNumber;

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String referenceNumber;
        private String fromAccountNumber;
        private String recipient;
        private BigDecimal amount;
        private String currency;
        private String type;
        private String status;
        private String description;
        private String billerName;
        private String billNumber;
        private String upiId;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }
}
