package com.lsp.atomic_payments.domain.ledger;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import java.time.Instant;

public record LedgerEntry(
    LedgerEntryId ledgerEntryId,
    AccountId accountId,
    PaymentId paymentId,
    Money amount,
    EntryType type,
    Instant createdAt) {

  public static LedgerPair createLedgerPair(
      AccountId from, AccountId to, PaymentId paymentId, Money amount, Instant createdAt) {

    return new LedgerPair(
        new LedgerEntry(LedgerEntryId.newId(), from, paymentId, amount, EntryType.DEBIT, createdAt),
        new LedgerEntry(LedgerEntryId.newId(), to, paymentId, amount, EntryType.CREDIT, createdAt));
  }
}
