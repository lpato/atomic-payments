package com.lsp.atomic_payments.domain.exception;

public class IdempotencyRecordException extends DomainException {

    public IdempotencyRecordException(String key, String hash) {
        super(String.format("Idempotency exception found for key %s and hash %s", key, hash));
    }

}
