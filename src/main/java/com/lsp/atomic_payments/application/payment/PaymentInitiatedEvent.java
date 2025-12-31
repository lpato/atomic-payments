package com.lsp.atomic_payments.application.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentInitiatedEvent(
                UUID paymentId,
                UUID fromAccountId,
                UUID toAccountId,
                BigDecimal amount,
                String currency,
                Instant createdAt) {
}
