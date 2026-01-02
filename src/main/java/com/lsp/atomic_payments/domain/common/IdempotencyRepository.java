package com.lsp.atomic_payments.domain.common;

import reactor.core.publisher.Mono;

public interface IdempotencyRepository {

  Mono<Idempotency> findByKey(String key);

  Mono<Void> save(Idempotency idempotency);
}
