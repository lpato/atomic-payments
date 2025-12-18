package com.lsp.atomic_payments.domain.ledger;

import java.util.List;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.payment.PaymentId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LedgerRepository {

    Mono<LedgerEntry> findById(LedgerEntryId ledgerEntryId);

    Flux<LedgerEntry> findByAccountId(AccountId accountId);

    Flux<LedgerEntry> findByPaymentId(PaymentId paymentId);

    Flux<LedgerEntry> saveAll(List<LedgerEntry> entries);
}
