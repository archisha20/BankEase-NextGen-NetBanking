package com.bankease.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankease.backend.dto.AccountRequest;
import com.bankease.backend.entity.Account;
import com.bankease.backend.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:4200")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(
            @RequestBody AccountRequest request) {

        Account account = accountService.createAccount(request);

        return ResponseEntity.ok(account);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Account>> getMyAccounts(
            Authentication authentication) {

        String email = authentication.getName();

        List<Account> accounts =
                accountService.getAccountsByEmail(email);

        return ResponseEntity.ok(accounts);
    }
}