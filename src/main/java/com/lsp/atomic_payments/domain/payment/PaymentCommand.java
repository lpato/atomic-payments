package com.lsp.atomic_payments.domain.payment;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;

public record PaymentCommand(
        AccountId fromAccountId,
        AccountId toAccountId,
        Money amount,
        String reference,
        String idempotencyKey // later
) {
}
