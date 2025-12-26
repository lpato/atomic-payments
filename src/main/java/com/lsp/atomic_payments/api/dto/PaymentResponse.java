package com.lsp.atomic_payments.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String status,
        BigDecimal amount,
        String currency,
        Instant createdAt) {
}
