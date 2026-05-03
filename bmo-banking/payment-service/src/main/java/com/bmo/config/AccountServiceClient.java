package com.bmo.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "account-service", fallback = AccountServiceFallback.class)
public interface AccountServiceClient {

    @PostMapping("/api/accounts/internal/debit")
    ResponseEntity<Object> debit(@RequestParam("accountNumber") String accountNumber,
                                  @RequestParam("amount") BigDecimal amount);
}
