package com.bmo.repository;

import com.bmo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    List<Transaction> findByFromAccountNumberOrToAccountNumberOrderByCreatedAtDesc(
            String fromAccount, String toAccount);
}
