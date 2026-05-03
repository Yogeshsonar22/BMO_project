package com.bmo.service;

import com.bmo.config.AccountServiceClient;
import com.bmo.dto.TransactionDto;
import com.bmo.entity.Transaction;
import com.bmo.exception.ResourceNotFoundException;
import com.bmo.repository.TransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "transferFallback")
    public TransactionDto.Response internalTransfer(TransactionDto.TransferRequest request) {
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setFromAccountNumber(request.getFromAccountNumber());
        transaction.setToAccountNumber(request.getToAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(Transaction.TransactionType.INTERNAL_TRANSFER);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setDescription(request.getDescription());

        Transaction saved = transactionRepository.save(transaction);

        try {
            // Debit from source
            accountServiceClient.debit(request.getFromAccountNumber(), request.getAmount());
            // Credit to destination
            accountServiceClient.credit(request.getToAccountNumber(), request.getAmount());

            saved.setStatus(Transaction.TransactionStatus.COMPLETED);
            saved.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            saved.setStatus(Transaction.TransactionStatus.FAILED);
            saved.setRemarks("Failed: " + e.getMessage());
            transactionRepository.save(saved);
            throw e;
        }

        return toResponse(transactionRepository.save(saved));
    }

    public TransactionDto.Response transferFallback(TransactionDto.TransferRequest request, Exception ex) {
        throw new RuntimeException("Transfer service temporarily unavailable. Please try again later.");
    }

    public TransactionDto.Response getTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        return toResponse(transaction);
    }

    public TransactionDto.Response getByReferenceNumber(String ref) {
        Transaction transaction = transactionRepository.findByReferenceNumber(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + ref));
        return toResponse(transaction);
    }

    public List<TransactionDto.Response> getAccountTransactions(String accountNumber) {
        return transactionRepository
                .findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransactionDto.Response toResponse(Transaction t) {
        TransactionDto.Response response = new TransactionDto.Response();
        response.setId(t.getId());
        response.setReferenceNumber(t.getReferenceNumber());
        response.setFromAccountNumber(t.getFromAccountNumber());
        response.setToAccountNumber(t.getToAccountNumber());
        response.setAmount(t.getAmount());
        response.setCurrency(t.getCurrency());
        response.setType(t.getType().name());
        response.setStatus(t.getStatus().name());
        response.setDescription(t.getDescription());
        response.setCreatedAt(t.getCreatedAt());
        response.setCompletedAt(t.getCompletedAt());
        return response;
    }
}
