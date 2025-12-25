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
import com.lsp.atomic_payments.domain.account.AccountVersion;
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
    private static final AccountVersion VERSION = new AccountVersion(0l);

    @BeforeEach
    void setUp() {

        accountFrom = new Account(AccountId.newId(), "test3", new Money(BigDecimal.valueOf(100),
                Currency.getInstance("EUR")), AccountStatus.ACTIVE, VERSION, NOW);
        accountTo = new Account(AccountId.newId(), "test3", new Money(BigDecimal.valueOf(30),
                Currency.getInstance("EUR")), AccountStatus.ACTIVE, VERSION, NOW);

    }

    @Test
    void testInitiatePayment() {

        Mono<Void> setUp = accountRepository.save(accountFrom)
                .then(accountRepository.save(accountTo))
                .then();

        Money toPay = new Money(BigDecimal.valueOf(20), Currency.getInstance("EUR"));

        PaymentCommand pay = new PaymentCommand(
                accountFrom.accountId(),
                accountTo.accountId(),
                toPay,
                "test",
                null);

        Mono<Payment> result = setUp.then(paymentService.initiatePayment(pay));

        StepVerifier.create(
                result.flatMap(payment -> Mono.zip(
                        paymentRepository.findById(payment.paymentId()),
                        ledgerRepository.findByPaymentId(payment.paymentId()).collectList(),
                        accountRepository.findById(payment.fromAccountId()),
                        accountRepository.findById(accountTo.accountId()))))
                .assertNext(tuple -> {

                    var payment = tuple.getT1();
                    var entries = tuple.getT2();
                    var from = tuple.getT3();
                    var to = tuple.getT4();

                    // payment assertions
                    assertThat(payment.amount().amount()).isEqualByComparingTo(toPay.amount());
                    assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);

                    // ledger assertions
                    assertThat(entries).hasSize(2);
                    assertThat(entries)
                            .extracting(LedgerEntry::type)
                            .containsExactlyInAnyOrder(
                                    EntryType.DEBIT,
                                    EntryType.CREDIT);

                    // account balances
                    assertThat(from.balance().amount())
                            .isEqualByComparingTo(BigDecimal.valueOf(80));

                    assertThat(to.balance().amount())
                            .isEqualByComparingTo(BigDecimal.valueOf(50));
                })
                .verifyComplete();
    }

}
