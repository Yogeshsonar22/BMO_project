package com.bmo.controller;

import com.bmo.dto.ApiResponse;
import com.bmo.dto.PaymentDto;
import com.bmo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/upi")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> upiPayment(
            @Valid @RequestBody PaymentDto.UpiRequest request) {
        PaymentDto.Response response = paymentService.processUpiPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("UPI payment successful", response));
    }

    @PostMapping("/bill")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> billPayment(
            @Valid @RequestBody PaymentDto.BillPaymentRequest request) {
        PaymentDto.Response response = paymentService.processBillPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bill payment successful", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", paymentService.getPayment(id)));
    }

    @GetMapping("/ref/{referenceNumber}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> getByRef(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved",
                paymentService.getByReferenceNumber(referenceNumber)));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<PaymentDto.Response>>> getAccountPayments(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved",
                paymentService.getAccountPayments(accountNumber)));
    }
}
