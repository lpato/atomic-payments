package com.lsp.atomic_payments.domain.ledger;

import java.util.UUID;

public record LedgerEntryId(UUID value) {

  public static LedgerEntryId newId() {
    return new LedgerEntryId(UUID.randomUUID());
  }
}
