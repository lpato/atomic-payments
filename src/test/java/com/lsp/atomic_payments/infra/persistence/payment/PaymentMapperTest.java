package com.lsp.atomic_payments.infra.persistence.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import com.lsp.atomic_payments.domain.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentMapperTest {

  private PaymentMapper mapper;

  static final PaymentId PAYMENT_ID = PaymentId.newId();
  static final AccountId FROM_ACCOUNT_ID = AccountId.newId();
  static final AccountId TO_ACCOUNT_ID = AccountId.newId();
  static final BigDecimal AMOUNT = BigDecimal.valueOf(10);
  static final Currency CURRENCY = Currency.getInstance("EUR");
  static final Money MONEY = new Money(AMOUNT, CURRENCY);
  static final PaymentStatus STATUS = PaymentStatus.PENDING;
  static final String REFERENCE = "reference";
  static final Instant CREATED_AT = Instant.now();

  @BeforeEach
  void setUp() {
    mapper = new PaymentMapper();
  }

  @Test
  void toEntity_shouldReturnNull_whenPaymentIsNull() {

    assertNull(mapper.toEntity(null));
  }

  @Test
  void toDomain_shouldReturnNull_whenEntityIsNull() {
    assertNull(mapper.toDomain(null));
  }

  @Test
  void toEntity_shouldMapFieldsCorrectly() {

    Payment payment =
        new Payment(
            PAYMENT_ID, FROM_ACCOUNT_ID, TO_ACCOUNT_ID, MONEY, STATUS, REFERENCE, CREATED_AT);

    PaymentEntity entity = mapper.toEntity(payment);

    assertEquals(entity.getId(), PAYMENT_ID.value());
    assertEquals(entity.getFromAccountId(), FROM_ACCOUNT_ID.value());
    assertEquals(entity.getToAccountId(), TO_ACCOUNT_ID.value());
    assertEquals(entity.getAmount(), AMOUNT);
    assertEquals(entity.getCurrency(), CURRENCY.getCurrencyCode());
    assertEquals(entity.getStatus(), STATUS.name());
    assertEquals(entity.getReference(), REFERENCE);
    assertEquals(entity.getCreatedAt(), CREATED_AT);
  }

  @Test
  void toDomain_shouldMapFieldsCorrectly() {

    PaymentEntity entity = new PaymentEntity();
    entity.setId(PAYMENT_ID.value());
    entity.setFromAccountId(FROM_ACCOUNT_ID.value());
    entity.setToAccountId(TO_ACCOUNT_ID.value());
    entity.setAmount(AMOUNT);
    entity.setCurrency(CURRENCY.getCurrencyCode());
    entity.setStatus(STATUS.name());
    entity.setReference(REFERENCE);
    entity.setCreatedAt(CREATED_AT);

    Payment payment = mapper.toDomain(entity);

    assertEquals(PAYMENT_ID, payment.paymentId());
    assertEquals(FROM_ACCOUNT_ID, payment.fromAccountId());
    assertEquals(TO_ACCOUNT_ID, payment.toAccountId());
    assertEquals(AMOUNT, payment.amount().amount());
    assertEquals(CURRENCY, payment.amount().currency());
    assertEquals(STATUS, payment.status());
    assertEquals(REFERENCE, payment.reference());
    assertEquals(CREATED_AT, payment.createdAt());
  }
}
