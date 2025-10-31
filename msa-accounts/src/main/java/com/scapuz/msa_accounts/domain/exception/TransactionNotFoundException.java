package com.scapuz.msa_accounts.domain.exception;

import java.util.UUID;

public class TransactionNotFoundException extends DomainException {
    public TransactionNotFoundException(Integer id) {
        super("Transaction not found with id: " + id);
    }

    public TransactionNotFoundException(String transactionNumber) {
        super("Transaction not found with number: " + transactionNumber);
    }
}
