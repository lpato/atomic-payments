package com.lsp.atomic_payments.api.payment;

import com.lsp.atomic_payments.api.dto.CreatePaymentRequest;
import com.lsp.atomic_payments.api.dto.PaymentResponse;
import com.lsp.atomic_payments.application.payment.IdempotentPaymentService;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;
import java.util.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final IdempotentPaymentService idempotentPaymentService;

  @PostMapping
  public Mono<ResponseEntity<PaymentResponse>> createPayment(
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody CreatePaymentRequest request) {

    return idempotentPaymentService
        .initiate(toCommand(request, idempotencyKey))
        .map(payment -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(payment)));
  }

  private PaymentCommand toCommand(CreatePaymentRequest req, String idempotencyKey) {
    return new PaymentCommand(
        new AccountId(req.fromAccountId()),
        new AccountId(req.toAccountId()),
        new Money(req.amount(), Currency.getInstance(req.currency())),
        req.reference(),
        idempotencyKey);
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
