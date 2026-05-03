package com.bmo.controller;

import com.bmo.dto.AccountDto;
import com.bmo.dto.ApiResponse;
import com.bmo.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountDto.Response>> createAccount(
            @Valid @RequestBody AccountDto.CreateRequest request) {
        AccountDto.Response account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDto.Response>> getAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Account retrieved", accountService.getAccount(id)));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountDto.Response>> getAccountByNumber(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success("Account retrieved",
                accountService.getAccountByNumber(accountNumber)));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<AccountDto.BalanceResponse>> getBalance(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success("Balance retrieved",
                accountService.getBalance(accountNumber)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AccountDto.Response>>> getUserAccounts(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved",
                accountService.getUserAccounts(userId)));
    }

    // Internal endpoints for inter-service calls
    @PostMapping("/internal/debit")
    public ResponseEntity<ApiResponse<Void>> debit(@RequestParam String accountNumber,
                                                    @RequestParam BigDecimal amount) {
        accountService.debit(accountNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Debit successful", null));
    }

    @PostMapping("/internal/credit")
    public ResponseEntity<ApiResponse<Void>> credit(@RequestParam String accountNumber,
                                                     @RequestParam BigDecimal amount) {
        accountService.credit(accountNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Credit successful", null));
    }
}
