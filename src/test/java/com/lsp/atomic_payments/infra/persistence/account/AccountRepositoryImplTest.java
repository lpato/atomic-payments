package com.lsp.atomic_payments.infra.persistence.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import({ AccountRepositoryImpl.class,
        AccountMapper.class })
class AccountRepositoryImplTest {

    static final AccountId ACCOUNT_ID = AccountId.newId();
    static final String OWNER = "user-123";
    static final BigDecimal BALANCE_AMOUNT = BigDecimal.valueOf(1000);
    static final Currency BALANCE_CURRENCY = Currency.getInstance("EUR");
    static final Money BALANCE = new Money(BALANCE_AMOUNT, BALANCE_CURRENCY);
    static final AccountStatus STATUS = AccountStatus.SUSPENDED;
    static final AccountVersion VERSION = new AccountVersion(0L);
    static final Instant CREATED_AT = Instant.parse("2024-01-01T10:00:00Z");

    @Autowired
    private AccountRepositoryImpl accountRepository;

    @Test
    void testFindAll() {

        // given
        AccountId accountId1 = AccountId.newId();
        Instant createdAt1 = Instant.now();
        Account account1 = new Account(accountId1, OWNER, BALANCE, STATUS, VERSION, createdAt1);

        AccountId accountId2 = AccountId.newId();
        Instant createdAt2 = Instant.now();
        Account account2 = new Account(accountId2, OWNER, BALANCE, STATUS, VERSION, createdAt2);

        Mono<Void> saved = accountRepository.save(account1)
                .then(accountRepository.save(account2))
                .then();

        // when
        Flux<Account> result = saved.thenMany(accountRepository.findAll());

        // then
        StepVerifier.create(result).recordWith(ArrayList::new)
                .thenConsumeWhile(a -> true) // consume ALL elements
                .consumeRecordedWith(accounts -> {
                    assertThat(accounts).extracting(Account::accountId).contains(accountId1, accountId2);
                }).verifyComplete();

    }

    @Test
    void testSaveAndFindById() {

        // given
        Account account = new Account(ACCOUNT_ID, OWNER, BALANCE, STATUS, VERSION, CREATED_AT);

        // when
        Mono<Account> result = accountRepository.save(account)
                .flatMap(saved -> accountRepository.findById(saved.accountId()));

        // then
        StepVerifier.create(result).assertNext(found -> {
            assertThat(found.accountId()).isEqualTo(ACCOUNT_ID);
            assertThat(found.balance().amount()).isEqualTo(BALANCE_AMOUNT);
            assertThat(found.balance().currency()).isEqualTo(BALANCE_CURRENCY);
            assertThat(found.owner()).isEqualTo(OWNER);
            assertThat(found.createdAt()).isEqualTo(CREATED_AT);
        });

    }

    @Test
    void saveFindByIdAndUpdate() {

        AccountId accountId = AccountId.newId();
        Account account = new Account(accountId, OWNER, BALANCE, STATUS, VERSION, CREATED_AT);

        Money debit = new Money(BigDecimal.valueOf(10), Currency.getInstance("EUR"));

        Mono<Account> result = accountRepository.save(account)
                // first read
                .flatMap(saved -> accountRepository.findById(saved.accountId()))
                // update domain object (IMMUTABLE)
                .map(found -> found.debit(debit))
                // persist update
                .flatMap(accountRepository::update)
                // read again to verify persistence
                .flatMap(saved -> accountRepository.findById(saved.accountId()));

        StepVerifier.create(result)
                .assertNext(updated -> {
                    assertThat(updated.accountId()).isEqualTo(accountId);
                    assertThat(updated.balance().amount()).isEqualByComparingTo("990");
                    assertThat(updated.balance().currency()).isEqualTo(BALANCE_CURRENCY);
                    assertThat(updated.owner()).isEqualTo(OWNER);
                    assertThat(updated.createdAt()).isEqualTo(CREATED_AT);
                })
                .verifyComplete();
    }

}
