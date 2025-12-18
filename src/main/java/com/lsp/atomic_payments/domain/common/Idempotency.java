package com.lsp.atomic_payments.domain.common;

import java.time.Instant;

public record Idempotency(
        String key,
        String requestHash,
        String responsePayload,
        Instant createdAt) {

}
