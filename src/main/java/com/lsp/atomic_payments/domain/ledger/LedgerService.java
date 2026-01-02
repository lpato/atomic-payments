package com.lsp.atomic_payments.domain.ledger;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LedgerService {

  private final LedgerRepository ledgerRepository;
}
