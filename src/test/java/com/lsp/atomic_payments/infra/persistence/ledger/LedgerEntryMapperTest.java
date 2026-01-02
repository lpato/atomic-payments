package com.lsp.atomic_payments.infra.persistence.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.ledger.EntryType;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerEntryId;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import org.junit.jupiter.api.Test;

public class LedgerEntryMapperTest {

  private final LedgerEntryMapper mapper = new LedgerEntryMapper();

  private static final LedgerEntryId LEDGER_ENTRY_ID = LedgerEntryId.newId();
  private static final AccountId ACCOUNT_ID = AccountId.newId();
  private static final PaymentId PAYMENT_ID = PaymentId.newId();
  private static final BigDecimal AMOUNT = BigDecimal.valueOf(10);
  private static final Currency CURRENCY = Currency.getInstance("EUR");
  private static final EntryType TYPE = EntryType.CREDIT;
  private static final Instant CREATED_AT = Instant.now();

  @Test
  void test_toDomain_toEntity() {

    LedgerEntryEntity entity = new LedgerEntryEntity();
    entity.setId(LEDGER_ENTRY_ID.value());
    entity.setAccountId(ACCOUNT_ID.value());
    entity.setPaymentId(PAYMENT_ID.value());
    entity.setAmount(AMOUNT);
    entity.setCurrency(CURRENCY.getCurrencyCode());
    entity.setType(TYPE.name().toUpperCase());
    entity.setCreatedAt(CREATED_AT);

    LedgerEntry ledgerEntry = mapper.toDomain(entity);

    assertEquals(LEDGER_ENTRY_ID, ledgerEntry.ledgerEntryId());
    assertEquals(ACCOUNT_ID, ledgerEntry.accountId());
    assertEquals(PAYMENT_ID, ledgerEntry.paymentId());
    assertEquals(AMOUNT, ledgerEntry.amount().amount());
    assertEquals(CURRENCY, ledgerEntry.amount().currency());
    assertEquals(TYPE, ledgerEntry.type());
    assertEquals(CREATED_AT, ledgerEntry.createdAt());

    LedgerEntryEntity mappedEntity = mapper.toEntity(ledgerEntry);

    assertEquals(entity.getId(), mappedEntity.getId());
    assertEquals(entity.getAccountId(), mappedEntity.getAccountId());
    assertEquals(entity.getPaymentId(), mappedEntity.getPaymentId());
    assertEquals(entity.getAmount(), mappedEntity.getAmount());
    assertEquals(entity.getCurrency(), mappedEntity.getCurrency());
    assertEquals(entity.getType(), mappedEntity.getType());
    assertEquals(entity.getCreatedAt(), mappedEntity.getCreatedAt());
  }

  @Test
  void testToEntity_shouldReturnNullEntity() {

    assertNull(mapper.toEntity(null));
  }

  @Test
  void testToDomain_shouldReturnNullEntity() {

    assertNull(mapper.toDomain(null));
  }
}
