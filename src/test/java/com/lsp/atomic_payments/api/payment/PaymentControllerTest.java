package com.lsp.atomic_payments.api.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lsp.atomic_payments.api.dto.CreatePaymentRequest;
import com.lsp.atomic_payments.application.payment.IdempotentPaymentService;
import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.exception.InsufficientFundsException;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import com.lsp.atomic_payments.domain.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import reactor.core.publisher.Mono;

@WebFluxTest(PaymentController.class)
class PaymentControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockitoBean IdempotentPaymentService idempotentPaymentService;

  private static final AccountVersion VERSION = new AccountVersion(0);
  private static final Instant NOW = Instant.now();

  Account from;
  Account to;
  Payment payment;

  @BeforeEach
  void setUp() {

    from =
        new Account(
            AccountId.newId(),
            "webTestFrom",
            new Money(BigDecimal.valueOf(200), Currency.getInstance("EUR")),
            AccountStatus.ACTIVE,
            VERSION,
            NOW);

    to =
        new Account(
            AccountId.newId(),
            "webTestTo",
            new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")),
            AccountStatus.ACTIVE,
            VERSION,
            NOW);

    payment =
        new Payment(
            PaymentId.newId(),
            from.accountId(),
            to.accountId(),
            new Money(BigDecimal.valueOf(10), Currency.getInstance("EUR")),
            PaymentStatus.PENDING,
            "web test reference",
            Instant.now());
  }

  @Test
  void testCreatePayment() {

    when(idempotentPaymentService.initiate(any())).thenReturn(Mono.just(payment));

    final CreatePaymentRequest request =
        new CreatePaymentRequest(
            from.accountId().value(),
            to.accountId().value(),
            BigDecimal.valueOf(10),
            "EUR",
            "web test reference",
            null);

    final ResponseSpec result = webTestClient.post().uri("/payments").bodyValue(request).exchange();

    result
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.paymentId")
        .exists()
        .jsonPath("$.status")
        .isEqualTo("PENDING")
        .jsonPath("$.amount")
        .isEqualTo(10)
        .jsonPath("$.currency")
        .isEqualTo("EUR");
  }

  @Test
  void shouldReturn409InsufficientFunds() {

    final CreatePaymentRequest request =
        new CreatePaymentRequest(
            from.accountId().value(),
            to.accountId().value(),
            BigDecimal.valueOf(500),
            "EUR",
            "web test reference",
            null);

    when(idempotentPaymentService.initiate(any()))
        .thenReturn(
            Mono.error(
                new InsufficientFundsException(
                    from.accountId(),
                    new Money(request.amount(), Currency.getInstance(request.currency())))));

    final ResponseSpec result = webTestClient.post().uri("/payments").bodyValue(request).exchange();

    result
        .expectStatus()
        .isEqualTo(HttpStatus.CONFLICT)
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo("INSUFFICIENT_FUNDS")
        .jsonPath("$.message")
        .isEqualTo(
            String.format(
                "Account %s has insufficient funds for %s %s payment",
                from.accountId(), request.amount(), request.currency()));
  }

  @Test
  void createPayment_withIdempotencyKey_shouldPassItToService() {

    // given
    when(idempotentPaymentService.initiate(any())).thenReturn(Mono.just(payment));

    final CreatePaymentRequest request =
        new CreatePaymentRequest(
            AccountId.newId().value(),
            AccountId.newId().value(),
            BigDecimal.valueOf(500),
            "EUR",
            "web test reference",
            null);

    // when payment is done with idempotent key in the header
    webTestClient
        .post()
        .uri("/payments")
        .header("Idempotency-Key", "key-123")
        .bodyValue(request)
        .exchange()
        .expectStatus()
        .isCreated();

    ArgumentCaptor<PaymentCommand> captor = ArgumentCaptor.forClass(PaymentCommand.class);
    verify(idempotentPaymentService).initiate(captor.capture());

    // assert the idempotent key was passed to the service
    assertThat(captor.getValue().idempotencyKey()).isEqualTo("key-123");
  }
}
