package com.bmo.service;

import com.bmo.config.AccountServiceClient;
import com.bmo.dto.PaymentDto;
import com.bmo.entity.Payment;
import com.bmo.exception.ResourceNotFoundException;
import com.bmo.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private SqsPublisherService sqsPublisherService;

    @InjectMocks
    private PaymentService paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testPayment = new Payment();
        testPayment.setId(1L);
        testPayment.setReferenceNumber("UPI-ABC12345");
        testPayment.setFromAccountNumber("BMO1111111111");
        testPayment.setRecipient("user@upi");
        testPayment.setUpiId("user@upi");
        testPayment.setAmount(new BigDecimal("200.00"));
        testPayment.setType(Payment.PaymentType.UPI);
        testPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        testPayment.setCurrency("CAD");
    }

    @Test
    void processUpiPayment_Success() {
        PaymentDto.UpiRequest request = new PaymentDto.UpiRequest(
                "BMO1111111111", "user@upi", new BigDecimal("200.00"), "Test UPI");

        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(accountServiceClient.debit(anyString(), any(BigDecimal.class)))
                .thenReturn(ResponseEntity.ok(null));
        doNothing().when(sqsPublisherService).publishNotification(any());

        PaymentDto.Response response = paymentService.processUpiPayment(request);

        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo("UPI");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        verify(accountServiceClient).debit("BMO1111111111", new BigDecimal("200.00"));
    }

    @Test
    void processBillPayment_Success() {
        Payment billPayment = new Payment();
        billPayment.setId(2L);
        billPayment.setReferenceNumber("BILL-XYZ98765");
        billPayment.setFromAccountNumber("BMO1111111111");
        billPayment.setRecipient("Rogers Telecom");
        billPayment.setBillerName("Rogers Telecom");
        billPayment.setBillNumber("INV-2023-001");
        billPayment.setAmount(new BigDecimal("150.00"));
        billPayment.setType(Payment.PaymentType.BILL_PAYMENT);
        billPayment.setStatus(Payment.PaymentStatus.COMPLETED);
        billPayment.setCurrency("CAD");

        PaymentDto.BillPaymentRequest request = new PaymentDto.BillPaymentRequest(
                "BMO1111111111", "Rogers Telecom", "INV-2023-001",
                new BigDecimal("150.00"), "Monthly bill");

        when(paymentRepository.save(any(Payment.class))).thenReturn(billPayment);
        when(accountServiceClient.debit(anyString(), any(BigDecimal.class)))
                .thenReturn(ResponseEntity.ok(null));
        doNothing().when(sqsPublisherService).publishNotification(any());

        PaymentDto.Response response = paymentService.processBillPayment(request);

        assertThat(response.getType()).isEqualTo("BILL_PAYMENT");
        assertThat(response.getBillerName()).isEqualTo("Rogers Telecom");
        verify(accountServiceClient).debit("BMO1111111111", new BigDecimal("150.00"));
    }

    @Test
    void getPayment_NotFound_ThrowsException() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment not found");
    }
}
