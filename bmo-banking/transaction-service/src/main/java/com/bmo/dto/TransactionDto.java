package com.bmo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequest {
        @NotBlank(message = "Source account is required")
        private String fromAccountNumber;

        @NotBlank(message = "Destination account is required")
        private String toAccountNumber;

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
        private String toAccountNumber;
        private BigDecimal amount;
        private String currency;
        private String type;
        private String status;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
    }
}
