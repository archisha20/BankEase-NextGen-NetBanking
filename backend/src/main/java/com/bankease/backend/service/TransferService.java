package com.bankease.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.bankease.backend.dto.TransferRequest;
import com.bankease.backend.dto.TransferResponse;

import com.bankease.backend.entity.Account;
import com.bankease.backend.entity.AccountStatus;
import com.bankease.backend.entity.Transaction;
import com.bankease.backend.entity.TransactionStatus;
import com.bankease.backend.entity.TransactionType;
import com.bankease.backend.entity.User;

import com.bankease.backend.repository.AccountRepository;
import com.bankease.backend.repository.TransactionRepository;
import com.bankease.backend.repository.UserRepository;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransferService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            UserRepository userRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransferResponse transfer(
            String email,
            TransferRequest request) {

        // Validate request
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transfer request is required");
        }

        if (request.getFromAccountNumber() == null
                || request.getFromAccountNumber().isBlank()
                || request.getRecipientAccountNumber() == null
                || request.getRecipientAccountNumber().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both account numbers are required");
        }

        if (request.getAmount() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transfer amount is required");
        }

        BigDecimal amount;

        try {
            amount = request.getAmount()
                    .setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum two decimal places allowed");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than zero");
        }

        // Get logged-in user using JWT email
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Logged-in user not found"));

        // Find sender account
        Account sender = accountRepository.findByAccountNumber(
                request.getFromAccountNumber().trim()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sender account not found"));

        // Verify sender belongs to logged-in user
        if (sender.getUser() == null
                || !sender.getUser().getId()
                        .equals(currentUser.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot transfer from another user's account");
        }

        // Find recipient account
        Account receiver = accountRepository.findByAccountNumber(
                request.getRecipientAccountNumber().trim()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Recipient account not found"));

        // Prevent self-transfer
        if (sender.getId().equals(receiver.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot transfer to the same account");
        }

        // Check account status
        if (sender.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sender account is inactive");
        }

        if (receiver.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recipient account is inactive");
        }

        // Check balance
        BigDecimal senderBalance = sender.getBalance();

        BigDecimal receiverBalance = receiver.getBalance();

        if (senderBalance == null) {
            senderBalance = BigDecimal.ZERO;
        }

        if (receiverBalance == null) {
            receiverBalance = BigDecimal.ZERO;
        }

        if (senderBalance.compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient balance");
        }

        // Validate description
        String description = request.getDescription();

        if (description == null || description.isBlank()) {
            description = "BankEase account transfer";
        } else {
            description = description.trim();

            if (description.length() > 255) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Description cannot exceed 255 characters");
            }
        }

        // Update balances
        sender.setBalance(senderBalance.subtract(amount));

        receiver.setBalance(receiverBalance.add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Create transaction record
        Transaction transaction = new Transaction();

        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(description);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // Return safe response DTO
        return new TransferResponse(
                savedTransaction.getId(),
                savedTransaction.getReferenceNumber(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getStatus(),
                savedTransaction.getDescription(),
                savedTransaction.getTransactionDate()
        );
    }
}