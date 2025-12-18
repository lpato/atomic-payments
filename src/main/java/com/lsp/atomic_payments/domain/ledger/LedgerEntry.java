package com.lsp.atomic_payments.domain.ledger;

import java.time.Instant;
import java.util.UUID;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;

public record LedgerEntry(
        LedgerEntryId ledgerEntryId,
        AccountId accountId,
        Money amount,
        EntryType type,
        UUID paymentId,
        Instant createdAt) {

}
