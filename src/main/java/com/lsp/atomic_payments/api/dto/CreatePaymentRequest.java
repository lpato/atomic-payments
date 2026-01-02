package com.lsp.atomic_payments.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
    @NotNull UUID fromAccountId,
    @NotNull UUID toAccountId,
    @Positive BigDecimal amount,
    @NotNull String currency,
    String reference,
    String idempotencyKey) {}
