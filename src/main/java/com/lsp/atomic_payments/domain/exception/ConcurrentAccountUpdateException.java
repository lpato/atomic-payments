package com.lsp.atomic_payments.domain.exception;

public class ConcurrentAccountUpdateException extends DomainException {
    public ConcurrentAccountUpdateException() {
        super("Concurrent update detected, please retry");
    }
}
