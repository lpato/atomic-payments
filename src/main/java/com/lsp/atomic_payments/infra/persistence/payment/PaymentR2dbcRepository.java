package com.lsp.atomic_payments.infra.persistence.payment;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PaymentR2dbcRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {

}
