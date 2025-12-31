package com.lsp.atomic_payments.domain.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lsp.atomic_payments.application.payment.IdempotentPaymentService;
import com.lsp.atomic_payments.application.payment.PaymentService;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import com.lsp.atomic_payments.domain.payment.PaymentStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class IdempotentPaymentServiceTest {

    @Mock
    PaymentService paymentService;

    @Mock
    IdempotencyRepository idempotencyRepository;

    @Mock
    TransactionalOperator transactionalOperator;

    @Mock
    IdempotencyUtils utils;

    @InjectMocks
    IdempotentPaymentService service;

    @BeforeEach
    void setUp() {

        when(transactionalOperator.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<Payment> callback = invocation.getArgument(0);
                    return Flux.from(callback.doInTransaction(mock(ReactiveTransaction.class)));
                });

    }

    @Test
    void initiate_shouldExecuteAndStore_whenNoExistingRecord() {

        PaymentCommand command = mock(PaymentCommand.class);
        Payment payment = mock(Payment.class);

        when(paymentService.initiatePayment(any()))
                .thenReturn(Mono.just(mock(Payment.class)));

        when(command.idempotencyKey()).thenReturn("key-1");

        // no existing record
        when(idempotencyRepository.findByKey("key-1"))
                .thenReturn(Mono.empty());

        // create payment
        when(paymentService.initiatePayment(command))
                .thenReturn(Mono.just(payment));

        when(utils.hash(command)).thenReturn("hash");
        when(utils.serialize(payment)).thenReturn("payload");

        // store idempotency record
        when(idempotencyRepository.save(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.initiate(command))
                .expectNext(payment)
                .verifyComplete();
    }

    @Test
    void inititate_shouldReturnExisting_whenRecordExists() {

        PaymentCommand command = mock(PaymentCommand.class);
        Idempotency idempotency = mock(Idempotency.class);
        Payment payment = mock(Payment.class);

        when(command.idempotencyKey()).thenReturn("key-1");
        when(idempotencyRepository.findByKey("key-1"))
                .thenReturn(Mono.just(idempotency));
        when(utils.hash(command)).thenReturn("hash");
        when(idempotency.requestHash()).thenReturn("hash");

        String payload = "serialized-payment";
        when(idempotency.responsePayload()).thenReturn(payload);
        when(utils.deserialize(payload)).thenReturn(payment);

        StepVerifier.create(service.initiate(command))
                .expectNext(payment)
                .verifyComplete();

        verify(paymentService, never()).initiatePayment(any());
        verify(idempotencyRepository, never()).save(any());

    }

}
