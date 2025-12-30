package com.lsp.atomic_payments.api.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import com.lsp.atomic_payments.api.dto.CreatePaymentRequest;
import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PaymentControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    AccountRepository accountRepository;

    private static final AccountVersion VERSION = new AccountVersion(0);
    private static final Instant NOW = Instant.now();

    Account from;
    Account to;

    @BeforeEach
    void setUp() {

        from = new Account(AccountId.newId(), "webTestFrom",
                new Money(BigDecimal.valueOf(200), Currency.getInstance("EUR")),
                AccountStatus.ACTIVE, VERSION, NOW);

        to = new Account(AccountId.newId(), "webTestTo",
                new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")),
                AccountStatus.ACTIVE, VERSION, NOW);

        accountRepository.save(from).then(accountRepository.save(to)).block();
    }

    @Test
    void testCreatePayment() {

        final CreatePaymentRequest request = new CreatePaymentRequest(
                from.accountId().value(),
                to.accountId().value(),
                BigDecimal.valueOf(10),
                "EUR",
                "web test reference",
                null);

        final ResponseSpec result = webTestClient.post().uri("/payments").bodyValue(request).exchange();

        result.expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.paymentId").exists()
                .jsonPath("$.status").isEqualTo("PENDING")
                .jsonPath("$.amount").isEqualTo(10)
                .jsonPath("$.currency").isEqualTo("EUR");

    }

    @Test
    void shouldReturn409InsufficientFunds() {

        final CreatePaymentRequest request = new CreatePaymentRequest(
                from.accountId().value(),
                to.accountId().value(),
                BigDecimal.valueOf(500),
                "EUR",
                "web test reference",
                null);

        final ResponseSpec result = webTestClient.post().uri("/payments").bodyValue(request).exchange();

        result.expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.code").isEqualTo("INSUFFICIENT_FUNDS")
                .jsonPath("$.message").isEqualTo(
                        String.format("Account %s has insufficient funds for %s %s payment",
                                from.accountId(), request.amount(), request.currency()));

    }

}
