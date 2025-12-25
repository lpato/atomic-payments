package com.lsp.atomic_payments.infra.persistence.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.ledger.EntryType;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerEntryId;
import com.lsp.atomic_payments.domain.ledger.LedgerRepository;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import com.lsp.atomic_payments.domain.payment.PaymentRepository;
import com.lsp.atomic_payments.domain.payment.PaymentStatus;
import com.lsp.atomic_payments.infra.persistence.account.AccountEntity;
import com.lsp.atomic_payments.infra.persistence.payment.PaymentEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class LedgerEntryRepositoryImplTest {

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private PaymentRepository paymentRepository;

        @Autowired
        private LedgerRepository ledgerEntryRepository;

        private Account account1;
        private Account account2;
        private Payment payment;
        private LedgerEntry ledger1;
        private LedgerEntry ledger2;

        private static final AccountVersion VERSION = new AccountVersion(0L);

        @BeforeEach
        private void setUp() {

                // given
                account1 = new Account(AccountId.newId(), "ledgertest",
                                new Money(BigDecimal.valueOf(10), Currency.getInstance("EUR")),
                                AccountStatus.SUSPENDED, VERSION, Instant.now());

                account2 = new Account(AccountId.newId(), "ledgertest",
                                new Money(BigDecimal.valueOf(10), Currency.getInstance("EUR")),
                                AccountStatus.SUSPENDED, VERSION, Instant.now());

                payment = new Payment(PaymentId.newId(), account1.accountId(), account2.accountId(),
                                new Money(BigDecimal.valueOf(5), Currency.getInstance("EUR")), PaymentStatus.PENDING,
                                "test",
                                Instant.now());

                ledger1 = new LedgerEntry(LedgerEntryId.newId(), account1.accountId(), payment.paymentId(),
                                payment.amount(), EntryType.CREDIT, Instant.now());
                ledger2 = new LedgerEntry(LedgerEntryId.newId(), account2.accountId(), payment.paymentId(),
                                payment.amount(), EntryType.DEBIT, Instant.now());

        }

        @Test
        void testSaveAll() {

                List<LedgerEntry> entries = List.of(ledger1, ledger2);

                // when
                Mono<Void> setUp = accountRepository.save(account1)
                                .then(accountRepository.save(account2)
                                                .then(paymentRepository.save(payment))
                                                .thenMany(ledgerEntryRepository.saveAll(entries)).then());

                Flux<LedgerEntry> result = setUp.thenMany(ledgerEntryRepository.findByPaymentId(payment.paymentId()));

                // then
                StepVerifier.create(result)
                                .recordWith(ArrayList::new)
                                .thenConsumeWhile(e -> true)
                                .consumeRecordedWith(found -> {
                                        assertThat(found).hasSize(2);
                                        assertThat(found)
                                                        .extracting(LedgerEntry::ledgerEntryId)
                                                        .contains(
                                                                        ledger1.ledgerEntryId(),
                                                                        ledger2.ledgerEntryId());
                                })
                                .verifyComplete();

        }
}
