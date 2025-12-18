package com.lsp.atomic_payments.domain.payment;

import java.time.Instant;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;

public record Payment(
        PaymentId paymentId,
        AccountId fromAccountId,
        AccountId toAccountId,
        Money amount,
        PaymentStatus status,
        String reference,
        Instant createdAt) {

}
