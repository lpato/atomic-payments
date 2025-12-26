package com.lsp.atomic_payments.api.payment;

import java.util.Currency;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lsp.atomic_payments.api.dto.CreatePaymentRequest;
import com.lsp.atomic_payments.api.dto.PaymentResponse;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;
import com.lsp.atomic_payments.domain.payment.PaymentService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Mono<ResponseEntity<PaymentResponse>> createPayment(@RequestBody CreatePaymentRequest request) {

        return paymentService.initiatePayment(toCommand(request))
                .map(payment -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(toResponse(payment)));
    }

    private PaymentCommand toCommand(CreatePaymentRequest req) {
        return new PaymentCommand(
                new AccountId(req.fromAccountId()),
                new AccountId(req.toAccountId()),
                new Money(req.amount(), Currency.getInstance(req.currency())),
                req.reference(),
                req.idempotencyKey());
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.paymentId().value(),
                payment.status().name(),
                payment.amount().amount(),
                payment.amount().currency().getCurrencyCode(),
                payment.createdAt());
    }

}
