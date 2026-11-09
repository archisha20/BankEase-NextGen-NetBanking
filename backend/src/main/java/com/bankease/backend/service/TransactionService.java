package com.bankease.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bankease.backend.dto.TransactionResponse;
import com.bankease.backend.entity.Account;
import com.bankease.backend.entity.Transaction;
import com.bankease.backend.entity.User;
import com.bankease.backend.repository.AccountRepository;
import com.bankease.backend.repository.TransactionRepository;
import com.bankease.backend.repository.UserRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getMyTransactions(String email) {

        // Find logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Get all accounts belonging to this user
        List<Account> accounts =
                accountRepository.findByUser_Id(user.getId());

        // Collect transactions
        List<Transaction> transactions = new ArrayList<>();

        for (Account account : accounts) {

            transactions.addAll(
                    transactionRepository
                            .findBySenderAccountId(account.getId())
            );

            transactions.addAll(
                    transactionRepository
                            .findByReceiverAccountId(account.getId())
            );
        }

        // Remove duplicates when a transaction involves
        // two accounts belonging to the same user.
        Map<Long, Transaction> uniqueTransactions =
                new LinkedHashMap<>();

        for (Transaction transaction : transactions) {
            uniqueTransactions.put(
                    transaction.getId(),
                    transaction
            );
        }

        // Sort newest transactions first
        List<Transaction> sortedTransactions =
                new ArrayList<>(uniqueTransactions.values());

        sortedTransactions.sort(
                Comparator.comparing(
                        Transaction::getTransactionDate,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        // Convert entities into response DTOs
        return sortedTransactions.stream()
                .map(transaction -> new TransactionResponse(

                        transaction.getId(),

                        transaction.getReferenceNumber(),

                        transaction.getAmount(),

                        transaction.getType(),

                        transaction.getStatus(),

                        transaction.getDescription(),

                        transaction.getTransactionDate(),

                        // Sender account number
                        transaction.getSenderAccount() == null
                                ? null
                                : transaction.getSenderAccount()
                                        .getAccountNumber(),

                        // Receiver account number
                        transaction.getReceiverAccount() == null
                                ? null
                                : transaction.getReceiverAccount()
                                        .getAccountNumber()

                ))
                .toList();
    }
}