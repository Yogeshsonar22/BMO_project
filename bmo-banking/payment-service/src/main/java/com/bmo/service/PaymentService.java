package com.bmo.service;

import com.bmo.config.AccountServiceClient;
import com.bmo.dto.NotificationMessage;
import com.bmo.dto.PaymentDto;
import com.bmo.entity.Payment;
import com.bmo.exception.ResourceNotFoundException;
import com.bmo.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Autowired
    private SqsPublisherService sqsPublisherService;

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "paymentFallback")
    public PaymentDto.Response processUpiPayment(PaymentDto.UpiRequest request) {
        Payment payment = new Payment();
        payment.setReferenceNumber("UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setFromAccountNumber(request.getFromAccountNumber());
        payment.setRecipient(request.getUpiId());
        payment.setUpiId(request.getUpiId());
        payment.setAmount(request.getAmount());
        payment.setType(Payment.PaymentType.UPI);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setDescription(request.getDescription());

        Payment saved = paymentRepository.save(payment);

        try {
            accountServiceClient.debit(request.getFromAccountNumber(), request.getAmount());
            saved.setStatus(Payment.PaymentStatus.COMPLETED);
            saved.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(saved);

            // Publish notification to SQS
            sqsPublisherService.publishNotification(new NotificationMessage(
                    request.getFromAccountNumber(),
                    "UPI Payment Successful",
                    String.format("Your UPI payment of %s %s to %s (Ref: %s) was successful.",
                            request.getAmount(), saved.getCurrency(), request.getUpiId(), saved.getReferenceNumber()),
                    "EMAIL"
            ));
        } catch (Exception e) {
            saved.setStatus(Payment.PaymentStatus.FAILED);
            saved.setRemarks("Failed: " + e.getMessage());
            paymentRepository.save(saved);
            throw e;
        }

        return toResponse(saved);
    }

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "paymentFallback")
    public PaymentDto.Response processBillPayment(PaymentDto.BillPaymentRequest request) {
        Payment payment = new Payment();
        payment.setReferenceNumber("BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setFromAccountNumber(request.getFromAccountNumber());
        payment.setRecipient(request.getBillerName());
        payment.setBillerName(request.getBillerName());
        payment.setBillNumber(request.getBillNumber());
        payment.setAmount(request.getAmount());
        payment.setType(Payment.PaymentType.BILL_PAYMENT);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setDescription(request.getDescription());

        Payment saved = paymentRepository.save(payment);

        try {
            accountServiceClient.debit(request.getFromAccountNumber(), request.getAmount());
            saved.setStatus(Payment.PaymentStatus.COMPLETED);
            saved.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(saved);

            // Publish notification to SQS
            sqsPublisherService.publishNotification(new NotificationMessage(
                    request.getFromAccountNumber(),
                    "Bill Payment Successful",
                    String.format("Your bill payment of %s %s to %s (Bill# %s, Ref: %s) was successful.",
                            request.getAmount(), saved.getCurrency(),
                            request.getBillerName(), request.getBillNumber(), saved.getReferenceNumber()),
                    "EMAIL"
            ));
        } catch (Exception e) {
            saved.setStatus(Payment.PaymentStatus.FAILED);
            saved.setRemarks("Failed: " + e.getMessage());
            paymentRepository.save(saved);
            throw e;
        }

        return toResponse(saved);
    }

    public PaymentDto.Response paymentFallback(Object request, Exception ex) {
        throw new RuntimeException("Payment service temporarily unavailable. Please try again later.");
    }

    public PaymentDto.Response getPayment(Long id) {
        return toResponse(paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id)));
    }

    public PaymentDto.Response getByReferenceNumber(String ref) {
        return toResponse(paymentRepository.findByReferenceNumber(ref)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + ref)));
    }

    public List<PaymentDto.Response> getAccountPayments(String accountNumber) {
        return paymentRepository.findByFromAccountNumberOrderByCreatedAtDesc(accountNumber)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PaymentDto.Response toResponse(Payment p) {
        PaymentDto.Response r = new PaymentDto.Response();
        r.setId(p.getId());
        r.setReferenceNumber(p.getReferenceNumber());
        r.setFromAccountNumber(p.getFromAccountNumber());
        r.setRecipient(p.getRecipient());
        r.setAmount(p.getAmount());
        r.setCurrency(p.getCurrency());
        r.setType(p.getType().name());
        r.setStatus(p.getStatus().name());
        r.setDescription(p.getDescription());
        r.setBillerName(p.getBillerName());
        r.setBillNumber(p.getBillNumber());
        r.setUpiId(p.getUpiId());
        r.setCreatedAt(p.getCreatedAt());
        r.setCompletedAt(p.getCompletedAt());
        return r;
    }
}
