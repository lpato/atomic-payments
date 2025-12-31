package com.lsp.atomic_payments.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import com.lsp.atomic_payments.application.payment.PaymentService;
import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.exception.ConcurrentAccountUpdateException;
import com.lsp.atomic_payments.domain.exception.CurrencyMismatchException;
import com.lsp.atomic_payments.domain.exception.InsufficientFundsException;
import com.lsp.atomic_payments.domain.ledger.EntryType;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

        @InjectMocks
        private PaymentService paymentService;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private PaymentRepository paymentRepository;

        @Mock
        private LedgerRepository ledgerRepository;

        @Captor
        ArgumentCaptor<Account> accountCaptor;

        Account accountFrom;
        Account accountTo;
        Account updateFrom;
        Account updateTo;

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

                // given
                Money toPay = new Money(BigDecimal.valueOf(20), Currency.getInstance("EUR"));

                PaymentCommand command = new PaymentCommand(
                                accountFrom.accountId(),
                                accountTo.accountId(),
                                toPay,
                                "test",
                                null);

                when(accountRepository.findById(accountFrom.accountId()))
                                .thenReturn(Mono.just(accountFrom));

                when(accountRepository.findById(accountTo.accountId()))
                                .thenReturn(Mono.just(accountTo));

                when(paymentRepository.save(any()))
                                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

                when(ledgerRepository.saveAll(any()))
                                .thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));

                when(accountRepository.update(any(Account.class)))
                                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

                // when
                Payment payment = paymentService.initiatePayment(command).block();

                verify(accountRepository, times(2)).update(accountCaptor.capture());

                List<Account> updatedAccounts = accountCaptor.getAllValues();

                Account updatedFrom = updatedAccounts.stream()
                                .filter(a -> a.accountId().equals(accountFrom.accountId()))
                                .findFirst()
                                .orElseThrow();

                Account updatedTo = updatedAccounts.stream()
                                .filter(a -> a.accountId().equals(accountTo.accountId()))
                                .findFirst()
                                .orElseThrow();

                // then (payment)
                assertThat(payment).isNotNull();
                assertThat(payment.amount().amount()).isEqualByComparingTo(toPay.amount());
                assertThat(payment.status()).isEqualTo(PaymentStatus.PENDING);

                // then (accounts)
                assertThat(updatedFrom.balance().amount())
                                .isEqualByComparingTo(BigDecimal.valueOf(80));

                assertThat(updatedTo.balance().amount())
                                .isEqualByComparingTo(BigDecimal.valueOf(50));

                // then (interactions)
                verify(paymentRepository).save(any(Payment.class));
                verify(ledgerRepository).saveAll(argThat(entries -> entries.size() == 2 &&
                                entries.stream().map(LedgerEntry::type)
                                                .collect(Collectors.toSet())
                                                .containsAll(Set.of(EntryType.DEBIT, EntryType.CREDIT))));
        }

        @Test
        void testInsufficientFunds() {

                Money toPay = new Money(BigDecimal.valueOf(500), Currency.getInstance("EUR"));

                PaymentCommand command = new PaymentCommand(
                                accountFrom.accountId(),
                                accountTo.accountId(),
                                toPay,
                                "test",
                                null);

                when(accountRepository.findById(accountFrom.accountId()))
                                .thenReturn(Mono.just(accountFrom));

                when(accountRepository.findById(accountTo.accountId()))
                                .thenReturn(Mono.just(accountTo));

                assertThrows(InsufficientFundsException.class, () -> {
                        paymentService.initiatePayment(command).block();
                });

                verifyNoInteractions(paymentRepository);
                verifyNoInteractions(ledgerRepository);
                verify(accountRepository, never()).update(any());

        }

        @Test
        void testCurrencyMismatch() {

                accountTo = new Account(AccountId.newId(), "test3", new Money(BigDecimal.valueOf(30),
                                Currency.getInstance("USD")), AccountStatus.ACTIVE, VERSION, NOW);

                Money toPay = new Money(BigDecimal.valueOf(500), Currency.getInstance("EUR"));

                PaymentCommand command = new PaymentCommand(
                                accountFrom.accountId(),
                                accountTo.accountId(),
                                toPay,
                                "test",
                                null);

                when(accountRepository.findById(accountFrom.accountId()))
                                .thenReturn(Mono.just(accountFrom));

                when(accountRepository.findById(accountTo.accountId()))
                                .thenReturn(Mono.just(accountTo));

                assertThrows(CurrencyMismatchException.class, () -> {
                        paymentService.initiatePayment(command).block();
                });

                verifyNoInteractions(paymentRepository);
                verifyNoInteractions(ledgerRepository);
                verify(accountRepository, never()).update(any());

        }

        @Test
        void shouldMapOptimisticLockingFailure() {

                Money toPay = new Money(BigDecimal.valueOf(10), Currency.getInstance("EUR"));

                PaymentCommand command = new PaymentCommand(
                                accountFrom.accountId(),
                                accountTo.accountId(),
                                toPay,
                                "test",
                                null);

                when(accountRepository.findById(accountFrom.accountId()))
                                .thenReturn(Mono.just(accountFrom));

                when(accountRepository.findById(accountTo.accountId()))
                                .thenReturn(Mono.just(accountTo));

                when(accountRepository.update(any()))
                                .thenReturn(Mono.error(new OptimisticLockingFailureException("conflict")));

                when(paymentRepository.save(any()))
                                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

                when(ledgerRepository.saveAll(any()))
                                .thenAnswer(inv -> Flux.fromIterable(inv.getArgument(0)));

                assertThrows(ConcurrentAccountUpdateException.class,
                                () -> paymentService.initiatePayment(command).block());

        }

}
