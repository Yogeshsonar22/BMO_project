package com.bmo.config;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountServiceFallback implements AccountServiceClient {

    @Override
    public ResponseEntity<Object> debit(String accountNumber, BigDecimal amount) {
        throw new RuntimeException("Account service is unavailable. Please try again later.");
    }
}
