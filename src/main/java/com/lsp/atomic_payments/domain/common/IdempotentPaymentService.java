package com.lsp.atomic_payments.domain.common;

import java.time.Instant;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.lsp.atomic_payments.domain.exception.IdempotencyRecordException;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;
import com.lsp.atomic_payments.domain.payment.PaymentService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IdempotentPaymentService {

    private final PaymentService paymentService;
    private final IdempotencyRepository idempotencyRepository;
    private final TransactionalOperator transactionalOperator;
    private final IdempotencyUtils utils;

    public Mono<Payment> initiate(PaymentCommand command) {

        if (command.idempotencyKey() == null) {
            return paymentService.initiatePayment(command);
        }

        return transactionalOperator.execute(
                tx -> idempotencyRepository.findByKey(command.idempotencyKey())
                        .flatMap(existing -> handleExisting(existing, command))
                        .switchIfEmpty(
                                Mono.defer(() -> executeAndStore(command)))// avoid eager call to store
                        .onErrorResume(DuplicateKeyException.class,
                                ex -> idempotencyRepository.findByKey(command.idempotencyKey())
                                        .flatMap(existing -> handleExisting(existing, command))))
                .single();

    }

    private Mono<Payment> executeAndStore(PaymentCommand command) {

        return paymentService.initiatePayment(command)
                .flatMap(payment -> {
                    Idempotency entity = new Idempotency(
                            command.idempotencyKey(),
                            utils.hash(command),
                            utils.serialize(payment), Instant.now());
                    return idempotencyRepository.save(entity).thenReturn(payment);
                });
    }

    private Mono<Payment> handleExisting(Idempotency existing, PaymentCommand command) {

        String newHash = utils.hash(command);

        if (!existing.requestHash().equals(newHash)) {
            return Mono.error(new IdempotencyRecordException(existing.key(), newHash));
        }

        return Mono.just(utils.deserialize(existing.responsePayload()));
    }

}
