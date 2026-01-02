package com.lsp.atomic_payments.infra.persistence.ledger;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerEntryId;
import com.lsp.atomic_payments.domain.ledger.LedgerRepository;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class LedgerEntryRepositoryImpl implements LedgerRepository {

  private final LedgerEntryR2dbcRepository ledgerEntryR2dbcRepository;
  private final LedgerEntryMapper mapper;

  @Override
  public Mono<LedgerEntry> findById(LedgerEntryId ledgerEntryId) {

    return ledgerEntryR2dbcRepository.findById(ledgerEntryId.value()).map(mapper::toDomain);
  }

  @Override
  public Flux<LedgerEntry> findByAccountId(AccountId accountId) {

    return ledgerEntryR2dbcRepository
        .findByAccountIdOrderByCreatedAtDesc(accountId.value())
        .map(mapper::toDomain);
  }

  @Override
  public Flux<LedgerEntry> findByPaymentId(PaymentId paymentId) {

    return ledgerEntryR2dbcRepository
        .findByPaymentIdOrderByCreatedAtDesc(paymentId.value())
        .map(mapper::toDomain);
  }

  @Override
  public Flux<LedgerEntry> saveAll(List<LedgerEntry> entries) {

    Flux<LedgerEntryEntity> entities = Flux.fromIterable(entries).map(mapper::toEntity);

    return ledgerEntryR2dbcRepository.saveAll(entities).map(mapper::toDomain);
  }
}
