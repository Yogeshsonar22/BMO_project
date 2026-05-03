package com.bmo.service;

import com.bmo.config.AccountServiceClient;
import com.bmo.dto.TransactionDto;
import com.bmo.entity.Transaction;
import com.bmo.exception.ResourceNotFoundException;
import com.bmo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setReferenceNumber("TXN-ABCD1234");
        testTransaction.setFromAccountNumber("BMO1111111111");
        testTransaction.setToAccountNumber("BMO2222222222");
        testTransaction.setAmount(new BigDecimal("500.00"));
        testTransaction.setType(Transaction.TransactionType.INTERNAL_TRANSFER);
        testTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        testTransaction.setCurrency("CAD");
    }

    @Test
    void internalTransfer_Success() {
        TransactionDto.TransferRequest request = new TransactionDto.TransferRequest(
                "BMO1111111111", "BMO2222222222", new BigDecimal("500.00"), "Test transfer");

        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountServiceClient.debit(anyString(), any(BigDecimal.class)))
                .thenReturn(ResponseEntity.ok(null));
        when(accountServiceClient.credit(anyString(), any(BigDecimal.class)))
                .thenReturn(ResponseEntity.ok(null));

        TransactionDto.Response response = transactionService.internalTransfer(request);

        assertThat(response).isNotNull();
        assertThat(response.getReferenceNumber()).isEqualTo("TXN-ABCD1234");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        verify(accountServiceClient).debit("BMO1111111111", new BigDecimal("500.00"));
        verify(accountServiceClient).credit("BMO2222222222", new BigDecimal("500.00"));
    }

    @Test
    void getTransaction_NotFound_ThrowsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found");
    }

    @Test
    void getByReferenceNumber_Success() {
        when(transactionRepository.findByReferenceNumber("TXN-ABCD1234"))
                .thenReturn(Optional.of(testTransaction));

        TransactionDto.Response response = transactionService.getByReferenceNumber("TXN-ABCD1234");

        assertThat(response.getReferenceNumber()).isEqualTo("TXN-ABCD1234");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void getAccountTransactions_Success() {
        when(transactionRepository.findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(
                "BMO1111111111", "BMO1111111111"))
                .thenReturn(Arrays.asList(testTransaction));

        List<TransactionDto.Response> list = transactionService.getAccountTransactions("BMO1111111111");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getFromAccountNumber()).isEqualTo("BMO1111111111");
    }
}
