package com.lsp.atomic_payments.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.ledger.EntryType;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    Account accountFrom;
    Account accountTo;

    private static final Instant NOW = Instant.parse("2025-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {

        accountFrom = new Account(AccountId.newId(), "test", new Money(BigDecimal.valueOf(100),
                Currency.getInstance("EUR")), AccountStatus.ACTIVE, NOW);
        accountTo = new Account(AccountId.newId(), "test", new Money(BigDecimal.valueOf(30),
                Currency.getInstance("EUR")), AccountStatus.ACTIVE, NOW);

    }

    @Test
    void testInitiatePayment() {

        Mono<Void> setUp = accountRepository.save(accountFrom)
                .then(accountRepository.save(accountTo))
                .then();

        Money toPay = new Money(BigDecimal.valueOf(20),
                Currency.getInstance("EUR"));
        PaymentCommand pay = new PaymentCommand(accountFrom.accountId(),
                accountTo.accountId(), toPay, "test", null);

        Mono<Payment> result = setUp.then(paymentService.initiatePayment(pay));

        StepVerifier.create(result)
                .assertNext(payment -> {

                    // assert payment persisted
                    StepVerifier.create(
                            paymentRepository.findById(payment.paymentId()))
                            .assertNext(found -> {
                                assertThat(found.amount()).isEqualTo(toPay);
                                assertThat(found.status()).isEqualTo(PaymentStatus.PENDING);
                            })
                            .verifyComplete();

                    // assert ledger entries
                    StepVerifier.create(
                            ledgerRepository.findByPaymentId(payment.paymentId()).collectList())
                            .assertNext(entries -> {
                                assertThat(entries).hasSize(2);
                                assertThat(entries)
                                        .extracting(LedgerEntry::type)
                                        .containsExactlyInAnyOrder(
                                                EntryType.DEBIT,
                                                EntryType.CREDIT);
                            })
                            .verifyComplete();

                    // asert accounts updated
                    StepVerifier.create(
                            accountRepository.findById(payment.fromAccountId())).assertNext(found -> {
                                assertThat(found.accountId().equals(accountFrom.accountId()));
                                assertThat(found.balance().amount().equals(BigDecimal.valueOf(80)));
                            }).verifyComplete();

                    StepVerifier.create(
                            accountRepository.findById(accountTo.accountId())).assertNext(found -> {
                                assertThat(found.accountId().equals(accountTo.accountId()));
                                assertThat(found.balance().amount().equals(BigDecimal.valueOf(50)));
                            }).verifyComplete();

                });

    }
}
