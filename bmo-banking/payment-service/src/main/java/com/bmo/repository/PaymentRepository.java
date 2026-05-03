package com.bmo.repository;

import com.bmo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReferenceNumber(String referenceNumber);
    List<Payment> findByFromAccountNumberOrderByCreatedAtDesc(String accountNumber);
    List<Payment> findByTypeOrderByCreatedAtDesc(Payment.PaymentType type);
}
