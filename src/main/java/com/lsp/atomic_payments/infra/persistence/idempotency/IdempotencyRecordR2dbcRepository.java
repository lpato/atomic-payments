package com.lsp.atomic_payments.infra.persistence.idempotency;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IdempotencyRecordR2dbcRepository
    extends ReactiveCrudRepository<IdempotencyRecordEntity, String> {}
