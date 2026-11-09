package com.bankease.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bankease.backend.dto.TransferRequest;
import com.bankease.backend.dto.TransferResponse;
import com.bankease.backend.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @RequestBody TransferRequest request,
            Authentication authentication) {

        TransferResponse response = transferService.transfer(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok(response);
    }
}