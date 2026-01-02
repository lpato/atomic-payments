package com.lsp.atomic_payments.infra.persistence.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import com.lsp.atomic_payments.domain.payment.PaymentRepository;
import com.lsp.atomic_payments.domain.payment.PaymentStatus;
import com.lsp.atomic_payments.infra.persistence.account.AccountMapper;
import com.lsp.atomic_payments.infra.persistence.account.AccountRepositoryImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({
  PaymentRepositoryImpl.class,
  AccountRepositoryImpl.class,
  PaymentMapper.class,
  AccountMapper.class
})
class PaymentRepositoryImplTest {

  static final PaymentId PAYMENT_ID = PaymentId.newId();
  static final AccountId FROM_ACCOUNT_ID = AccountId.newId();
  static final AccountId TO_ACCOUNT_ID = AccountId.newId();
  static final BigDecimal AMOUNT = BigDecimal.valueOf(10);
  static final Currency CURRENCY = Currency.getInstance("EUR");
  static final Money MONEY = new Money(AMOUNT, CURRENCY);
  static final PaymentStatus STATUS = PaymentStatus.PENDING;
  static final String REFERENCE = "reference";
  static final Instant CREATED_AT = Instant.now();

  static final String OWNER = "user-123";
  static final BigDecimal BALANCE_AMOUNT = BigDecimal.valueOf(1000);
  static final Currency BALANCE_CURRENCY = Currency.getInstance("EUR");
  static final Money BALANCE = new Money(BALANCE_AMOUNT, BALANCE_CURRENCY);

  private static final AccountVersion VERSION = new AccountVersion(0L);

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private AccountRepository accountRepository;

  @Test
  void testSaveAndRetrievePayment() {

    AccountId accountId1 = AccountId.newId();
    Instant createdAt1 = Instant.now();
    Account account1 =
        new Account(accountId1, OWNER, BALANCE, AccountStatus.SUSPENDED, VERSION, createdAt1);

    AccountId accountId2 = AccountId.newId();
    Instant createdAt2 = Instant.now();
    Account account2 =
        new Account(accountId2, OWNER, BALANCE, AccountStatus.SUSPENDED, VERSION, createdAt2);

    Mono<Account> setup = accountRepository.save(account1).then(accountRepository.save(account2));

    Payment payment =
        new Payment(
            PAYMENT_ID,
            account1.accountId(),
            account2.accountId(),
            MONEY,
            STATUS,
            REFERENCE,
            CREATED_AT);

    Mono<Payment> result = setup.then(paymentRepository.save(payment));

    StepVerifier.create(result)
        .assertNext(
            found -> {
              assertThat(found.paymentId()).isEqualTo(PAYMENT_ID);
              assertThat(found.fromAccountId()).isEqualTo(accountId1);
              assertThat(found.toAccountId()).isEqualTo(accountId2);
              assertThat(found.amount().amount()).isEqualTo(AMOUNT);
              assertThat(found.amount().currency()).isEqualTo(CURRENCY);
              assertThat(found.reference()).isEqualTo(REFERENCE);
              assertThat(found.createdAt()).isEqualTo(CREATED_AT);
              assertThat(found.status()).isEqualTo(STATUS);
            })
        .verifyComplete();
  }
}
