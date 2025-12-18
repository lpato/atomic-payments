package com.lsp.atomic_payments.domain.ledger;

import java.time.Instant;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.PaymentId;

public record LedgerEntry(
        LedgerEntryId ledgerEntryId,
        AccountId accountId,
        PaymentId paymentId,
        Money amount,
        EntryType type,
        Instant createdAt) {

}
