package com.lsp.atomic_payments.infra.persistence.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.common.Money;

class AccountMapperTest {

    private AccountMapper mapper;

    static final AccountId ACCOUNT_ID = AccountId.newId();
    static final String OWNER = "user-123";
    static final BigDecimal BALANCE_AMOUNT = BigDecimal.valueOf(1000);
    static final Currency BALANCE_CURRENCY = Currency.getInstance("EUR");
    static final Money BALANCE = new Money(BALANCE_AMOUNT, BALANCE_CURRENCY);
    static final AccountStatus STATUS = AccountStatus.SUSPENDED;
    static final Instant CREATED_AT = Instant.parse("2024-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        mapper = new AccountMapper();
    }

    @Test
    void toEntity_shouldReturnNull_whenAccountIsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void toEntity_shouldMapFieldsCorrectly() {

        Account account = new Account(
                ACCOUNT_ID,
                OWNER,
                BALANCE,
                STATUS,
                CREATED_AT);

        AccountEntity entity = mapper.toEntity(account);

        assertEquals(ACCOUNT_ID.value(), entity.getId());
        assertEquals(OWNER, entity.getOwner());
        assertEquals(BALANCE_AMOUNT, entity.getBalanceAmount());
        assertEquals(BALANCE_CURRENCY.getCurrencyCode(), entity.getBalanceCurrency());
        assertEquals(STATUS.name(), entity.getStatus());
        assertEquals(CREATED_AT, entity.getCreatedAt());
    }

    @Test
    void toDomain_shouldMapFieldsCorrectly() {

        AccountEntity entity = new AccountEntity();
        entity.setId(ACCOUNT_ID.value());
        entity.setOwner(OWNER);
        entity.setBalanceAmount(BALANCE_AMOUNT);
        entity.setBalanceCurrency(BALANCE_CURRENCY.getCurrencyCode());
        entity.setStatus(STATUS.name());
        entity.setCreatedAt(CREATED_AT);

        Account account = mapper.toDomain(entity);

        assertEquals(ACCOUNT_ID, account.accountId());
        assertEquals(OWNER, account.owner());
        assertEquals(BALANCE_AMOUNT, account.balance().amount());
        assertEquals(BALANCE_CURRENCY, account.balance().currency());
        assertEquals(STATUS, account.status());
        assertEquals(CREATED_AT, account.createdAt());
    }

    @Test
    void shouldMapDomainToEntityAndBackWithoutLosingInformation() {

        Account original = new Account(
                ACCOUNT_ID,
                OWNER,
                BALANCE,
                STATUS,
                CREATED_AT);

        AccountEntity entity = mapper.toEntity(original);
        Account mappedBack = mapper.toDomain(entity);

        assertEquals(original.accountId(), mappedBack.accountId());
        assertEquals(original.owner(), mappedBack.owner());
        assertEquals(original.balance().amount(), mappedBack.balance().amount());
        assertEquals(original.balance().currency(), mappedBack.balance().currency());
        assertEquals(original.status(), mappedBack.status());
        assertEquals(original.createdAt(), mappedBack.createdAt());
    }
}
