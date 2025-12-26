package com.lsp.atomic_payments.domain.exception;

public class CurrencyMismatchException extends DomainException {
    public CurrencyMismatchException() {
        super("Accounts and payment must have the same currency");
    }
}
