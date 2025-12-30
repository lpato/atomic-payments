package com.lsp.atomic_payments.infra.idempotency;

import org.springframework.stereotype.Repository;

import com.lsp.atomic_payments.domain.common.Idempotency;
import com.lsp.atomic_payments.domain.common.IdempotencyRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class IdempotencyRecordRepositoryImpl implements IdempotencyRepository {

    private final IdempotencyRecordR2dbcRepository idempotencyRecordR2dbcRepository;

    @Override
    public Mono<Idempotency> findByKey(String key) {

        Mono<IdempotencyRecordEntity> result = idempotencyRecordR2dbcRepository.findById(key);

        return result.map(entity -> {
            return new Idempotency(entity.getIdempotencyKey(), entity.getRequestHash(), entity.getResponsePayload(),
                    entity.getCreatedAt());
        });
    }

    @Override
    public Mono<Void> save(Idempotency idempotency) {

        IdempotencyRecordEntity entity = new IdempotencyRecordEntity(idempotency.key(),
                idempotency.requestHash(),
                idempotency.responsePayload(),
                idempotency.createdAt());

        return idempotencyRecordR2dbcRepository.save(entity).then();
    }

}
