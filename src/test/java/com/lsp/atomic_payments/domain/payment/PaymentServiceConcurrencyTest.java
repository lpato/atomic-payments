package com.lsp.atomic_payments.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.lsp.atomic_payments.application.payment.PaymentService;
import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.exception.ConcurrentAccountUpdateException;
import com.lsp.atomic_payments.infra.persistence.account.AccountMapper;
import com.lsp.atomic_payments.infra.persistence.account.AccountRepositoryImpl;
import com.lsp.atomic_payments.infra.persistence.ledger.LedgerEntryMapper;
import com.lsp.atomic_payments.infra.persistence.ledger.LedgerEntryRepositoryImpl;
import com.lsp.atomic_payments.infra.persistence.payment.PaymentMapper;
import com.lsp.atomic_payments.infra.persistence.payment.PaymentRepositoryImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({
  AccountRepositoryImpl.class,
  AccountMapper.class,
  PaymentService.class,
  PaymentRepositoryImpl.class,
  PaymentMapper.class,
  LedgerEntryRepositoryImpl.class,
  LedgerEntryMapper.class
})
class PaymentServiceConcurrencyTest {

  @Autowired private PaymentService paymentService;

  @Autowired private AccountRepository accountRepository;

  Account accountFrom;
  Account accountTo;
  Account updateFrom;
  Account updateTo;

  private static final Instant NOW = Instant.parse("2025-01-01T10:00:00Z");
  private static final AccountVersion VERSION = new AccountVersion(0l);

  @BeforeEach
  void setUp() {

    accountFrom =
        new Account(
            AccountId.newId(),
            "test3",
            new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")),
            AccountStatus.ACTIVE,
            VERSION,
            NOW);
    accountTo =
        new Account(
            AccountId.newId(),
            "test3",
            new Money(BigDecimal.valueOf(30), Currency.getInstance("EUR")),
            AccountStatus.ACTIVE,
            VERSION,
            NOW);
  }

  @Test
  void shouldFailOnePaymentOnConcurrentAccountUpdate() {

    Account source =
        new Account(
            AccountId.newId(),
            "source",
            new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")),
            AccountStatus.ACTIVE,
            VERSION,
            NOW);

    Account target1 =
        new Account(
            AccountId.newId(),
            "target1",
            new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")),
            AccountStatus.ACTIVE,
            VERSION,
            NOW);
    Account target2 =
        new Account(
            AccountId.newId(),
            "target2",
            new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR")),
            AccountStatus.ACTIVE,
            VERSION,
            NOW);

    PaymentCommand command1 =
        new PaymentCommand(
            source.accountId(),
            target1.accountId(),
            new Money(BigDecimal.valueOf(60), Currency.getInstance("EUR")),
            "p1",
            null);

    PaymentCommand command2 =
        new PaymentCommand(
            source.accountId(),
            target2.accountId(),
            new Money(BigDecimal.valueOf(60), Currency.getInstance("EUR")),
            "p2",
            null);

    Mono<Void> setup =
        accountRepository
            .save(source)
            .then(accountRepository.save(target1))
            .then(accountRepository.save(target2))
            .then();

    Mono<Void> concurrent =
        setup.then(
            Mono.whenDelayError(
                paymentService.initiatePayment(command1),
                paymentService.initiatePayment(command2)));

    StepVerifier.create(concurrent).expectError(ConcurrentAccountUpdateException.class).verify();

    StepVerifier.create(accountRepository.findById(source.accountId()))
        .assertNext(acc -> assertThat(acc.balance().amount()).isEqualByComparingTo("40"))
        .verifyComplete();
  }
}
