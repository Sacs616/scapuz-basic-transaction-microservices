package com.scapuz.msa_accounts.infrastructure.adapter.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scapuz.msa_accounts.application.port.input.AccountService;
import com.scapuz.msa_accounts.domain.model.Transaction;
import com.scapuz.msa_accounts.infrastructure.adapter.rest.dto.TransactionRequest;
import com.scapuz.msa_accounts.infrastructure.adapter.rest.dto.TransactionResponse;
import com.scapuz.msa_accounts.infrastructure.adapter.rest.mapper.TransactionDtoMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
public class TransactionController {

    private final AccountService accountService;
    private final TransactionDtoMapper dtoMapper;

    @PostMapping
    @Operation(summary = "Create transaction", description = "Creates a new transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {

        log.info("REST request to create transaction: {}", request.getTransactionNumber());

        Transaction transaction = dtoMapper.toDomain(request);
        Transaction created = accountService.createTransaction(transaction);
        TransactionResponse response = dtoMapper.toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<TransactionResponse> getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable Integer id) {

        log.info("REST request to get transaction: {}", id);

        Transaction transaction = accountService.getTransactionById(id);
        TransactionResponse response = dtoMapper.toResponse(transaction);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transactions by account ID")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(
            @Parameter(description = "Account ID") @PathVariable Integer accountId) {

        log.info("REST request to get transactions for account: {}", accountId);

        List<Transaction> transactions = accountService.getTransactionsByAccountId(accountId);
        List<TransactionResponse> responses = transactions.stream()
                .map(dtoMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}