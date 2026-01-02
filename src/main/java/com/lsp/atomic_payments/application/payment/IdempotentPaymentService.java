package com.lsp.atomic_payments.application.payment;

import com.lsp.atomic_payments.domain.common.Idempotency;
import com.lsp.atomic_payments.domain.common.IdempotencyRepository;
import com.lsp.atomic_payments.domain.common.IdempotencyUtils;
import com.lsp.atomic_payments.domain.exception.IdempotencyRecordException;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotentPaymentService {

  private final PaymentService paymentService;
  private final IdempotencyRepository idempotencyRepository;
  private final TransactionalOperator transactionalOperator;
  private final IdempotencyUtils utils;
  private final ApplicationEventPublisher eventPublisher;

  public Mono<Payment> initiate(PaymentCommand command) {

    return transactionalOperator
        .execute(tx -> handleIdemponecyOrDirect(command))
        .single()
        .doOnSuccess(this::publishPaymentInitiated);
  }

  private Mono<Payment> handleIdemponecyOrDirect(PaymentCommand command) {
    if (command.idempotencyKey() == null) {
      return paymentService.initiatePayment(command);
    }
    return handleIdempotency(command);
  }

  private Mono<Payment> handleIdempotency(PaymentCommand command) {

    return idempotencyRepository
        .findByKey(command.idempotencyKey())
        .flatMap(existing -> handleExisting(existing, command))
        .switchIfEmpty(Mono.defer(() -> executeAndStore(command))) // avoid eager call to store
        .onErrorResume(
            DuplicateKeyException.class,
            ex ->
                idempotencyRepository
                    .findByKey(command.idempotencyKey())
                    .flatMap(existing -> handleExisting(existing, command)));
  }

  private void publishPaymentInitiated(Payment payment) {
    eventPublisher.publishEvent(
        new PaymentInitiatedEvent(
            payment.paymentId().value(),
            payment.fromAccountId().value(),
            payment.toAccountId().value(),
            payment.amount().amount(),
            payment.amount().currency().getCurrencyCode(),
            payment.createdAt()));
  }

  Mono<Payment> executeAndStore(PaymentCommand command) {
    return paymentService
        .initiatePayment(command)
        .flatMap(
            payment -> idempotencyRepository.save(toRecord(command, payment)).thenReturn(payment));
  }

  private Idempotency toRecord(PaymentCommand command, Payment payment) {

    return new Idempotency(
        command.idempotencyKey(), utils.hash(command), utils.serialize(payment), Instant.now());
  }

  private Mono<Payment> handleExisting(Idempotency existing, PaymentCommand command) {

    String newHash = utils.hash(command);

    if (!existing.requestHash().equals(newHash)) {
      return Mono.error(new IdempotencyRecordException(existing.key(), newHash));
    }

    return Mono.just(utils.deserialize(existing.responsePayload()));
  }
}
