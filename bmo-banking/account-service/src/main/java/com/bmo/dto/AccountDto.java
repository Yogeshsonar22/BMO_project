package com.bmo.dto;

import com.bmo.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull
        private Long userId;
        @NotNull
        private Account.AccountType accountType;
        private String currency = "CAD";
        private BigDecimal initialDeposit = BigDecimal.ZERO;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String accountNumber;
        private String accountType;
        private BigDecimal balance;
        private String status;
        private String currency;
        private String ownerName;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BalanceResponse {
        private String accountNumber;
        private BigDecimal balance;
        private String currency;
        private String status;
    }
}
