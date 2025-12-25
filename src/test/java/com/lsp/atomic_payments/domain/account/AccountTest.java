package com.lsp.atomic_payments.domain.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import org.junit.jupiter.api.Test;

import com.lsp.atomic_payments.domain.common.Money;

class AccountTest {

    private static final Instant NOW = Instant.parse("2025-01-01T10:00:00Z");
    private static final String OWNER = "test123";
    private static final Money MONEY_100EUR = new Money(BigDecimal.valueOf(100), Currency.getInstance("EUR"));
    private static final Money MONEY_30EUR = new Money(BigDecimal.valueOf(30), Currency.getInstance("EUR"));
    private static final Money MONEY_70EUR = new Money(BigDecimal.valueOf(70), Currency.getInstance("EUR"));
    private static final Money MONEY_50EUR = new Money(BigDecimal.valueOf(50), Currency.getInstance("EUR"));
    private static final Money MONEY_150EUR = new Money(BigDecimal.valueOf(150), Currency.getInstance("EUR"));
    private static final AccountVersion VERSION = new AccountVersion(0l);

    @Test
    void isActive_returnsTrue_whenStatusIsActive() {
        Account account = new Account(
                AccountId.newId(),
                OWNER,
                MONEY_100EUR,
                AccountStatus.ACTIVE,
                VERSION,
                NOW);

        assertThat(account.isActive()).isTrue();
    }

    @Test
    void isActive_returnsFalse_whenStatusIsNotActive() {
        Account account = new Account(
                AccountId.newId(),
                OWNER,
                MONEY_100EUR,
                AccountStatus.SUSPENDED,
                VERSION,
                NOW);

        assertThat(account.isActive()).isFalse();
    }

    @Test
    void debit_subtractsAmountFromBalance_andReturnsNewAccount() {
        Account account = new Account(
                AccountId.newId(),
                OWNER,
                MONEY_100EUR,
                AccountStatus.ACTIVE,
                VERSION,
                NOW);

        Account debited = account.debit(MONEY_30EUR);

        assertThat(debited.balance()).isEqualTo(MONEY_70EUR);
        assertThat(debited).isNotSameAs(account);

        // unchanged fields
        assertThat(debited.accountId()).isEqualTo(account.accountId());
        assertThat(debited.owner()).isEqualTo(account.owner());
        assertThat(debited.status()).isEqualTo(account.status());
        assertThat(debited.createdAt()).isEqualTo(account.createdAt());
    }

    @Test
    void credit_addsAmountToBalance_andReturnsNewAccount() {
        Account account = new Account(
                AccountId.newId(),
                OWNER,
                MONEY_100EUR,
                AccountStatus.ACTIVE,
                VERSION,
                NOW);

        Account credited = account.credit(MONEY_50EUR);

        assertThat(credited.balance()).isEqualTo(MONEY_150EUR);
        assertThat(credited).isNotSameAs(account);

        // unchanged fields
        assertThat(credited.accountId()).isEqualTo(account.accountId());
        assertThat(credited.owner()).isEqualTo(account.owner());
        assertThat(credited.status()).isEqualTo(account.status());
        assertThat(credited.createdAt()).isEqualTo(account.createdAt());
    }
}
