package com.bmo.controller;

import com.bmo.dto.ApiResponse;
import com.bmo.dto.TransactionDto;
import com.bmo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionDto.Response>> transfer(
            @Valid @RequestBody TransactionDto.TransferRequest request) {
        TransactionDto.Response response = transactionService.internalTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer successful", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDto.Response>> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved",
                transactionService.getTransaction(id)));
    }

    @GetMapping("/ref/{referenceNumber}")
    public ResponseEntity<ApiResponse<TransactionDto.Response>> getByRef(
            @PathVariable String referenceNumber) {
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved",
                transactionService.getByReferenceNumber(referenceNumber)));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionDto.Response>>> getAccountTransactions(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved",
                transactionService.getAccountTransactions(accountNumber)));
    }
}
