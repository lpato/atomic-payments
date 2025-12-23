package com.lsp.atomic_payments.domain.ledger;

public record LedgerPair(
        LedgerEntry debit,
        LedgerEntry credit) {

}
