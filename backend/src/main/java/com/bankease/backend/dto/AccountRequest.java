package com.bankease.backend.dto;

import com.bankease.backend.entity.AccountType;

public class AccountRequest {

    private Long userId;
    private AccountType accountType;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}