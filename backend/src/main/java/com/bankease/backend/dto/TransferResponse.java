package com.bankease.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bankease.backend.entity.TransactionStatus;
import com.bankease.backend.entity.TransactionType;

public class TransferResponse {

    private Long id;
    private String referenceNumber;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private LocalDateTime transactionDate;

    public TransferResponse(
            Long id,
            String referenceNumber,
            BigDecimal amount,
            TransactionType type,
            TransactionStatus status,
            String description,
            LocalDateTime transactionDate) {

        this.id = id;
        this.referenceNumber = referenceNumber;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public Long getId() {
        return id;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}