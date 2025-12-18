package com.lsp.atomic_payments.domain.payment;

import reactor.core.publisher.Mono;

public interface PaymentRepository {

    Mono<Payment> findById(PaymentId paymentId);

    Mono<Payment> save(Payment payment);

}
