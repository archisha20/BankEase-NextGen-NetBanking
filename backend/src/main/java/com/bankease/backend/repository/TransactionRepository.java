package com.bankease.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bankease.backend.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderAccountId(Long accountId);

    List<Transaction> findByReceiverAccountId(Long accountId);
}