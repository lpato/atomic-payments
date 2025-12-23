package com.lsp.atomic_payments.infra.persistence.ledger;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface LedgerEntryR2dbcRepository extends ReactiveCrudRepository<LedgerEntryEntity, UUID> {

    @Query("SELECT * FROM ledger_entries WHERE account_id = :id ORDER BY created_at DESC")
    public Flux<LedgerEntryEntity> findByAccountIdOrderByCreatedAtDesc(@Param("id") UUID id);

    @Query("SELECT * FROM ledger_entries WHERE payment_id = :id ORDER BY created_at DESC")
    public Flux<LedgerEntryEntity> findByPaymentIdOrderByCreatedAtDesc(@Param("id") UUID id);

}
