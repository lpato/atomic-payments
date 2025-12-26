package com.lsp.atomic_payments.domain.payment;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.exception.AccountNotActiveException;
import com.lsp.atomic_payments.domain.exception.ConcurrentAccountUpdateException;
import com.lsp.atomic_payments.domain.exception.CurrencyMismatchException;
import com.lsp.atomic_payments.domain.exception.InsufficientFundsException;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerPair;
import com.lsp.atomic_payments.domain.ledger.LedgerRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionalOperator transactionalOperator;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    public Mono<Payment> initiatePayment(PaymentCommand command) {

        Mono<Payment> logic = Mono.zip(

                accountRepository.findById(command.fromAccountId()),
                accountRepository.findById(command.toAccountId()))

                .flatMap(tuple -> {
                    Account fromAccount = tuple.getT1();
                    Account toAccount = tuple.getT2();

                    // validation
                    if (!fromAccount.isActive()) {
                        return Mono.error(new AccountNotActiveException(fromAccount.accountId()));
                    }
                    if (!toAccount.isActive()) {
                        return Mono.error(new AccountNotActiveException(toAccount.accountId()));
                    }
                    if (!fromAccount.balance().currency().equals(toAccount.balance().currency())) {
                        return Mono.error(new CurrencyMismatchException());
                    }
                    if (fromAccount.balance().amount().compareTo(command.amount().amount()) < 0) {
                        return Mono.error(new InsufficientFundsException(fromAccount.accountId(), command.amount()));
                    }

                    Payment payment = Payment.initiate(command);

                    LedgerPair ledgerPair = LedgerEntry.createLedgerPair(
                            fromAccount.accountId(),
                            toAccount.accountId(),
                            payment.paymentId(),
                            payment.amount(),
                            Instant.now());

                    Account fromUpdated = fromAccount.debit(payment.amount());
                    Account toUpdated = toAccount.credit(payment.amount());

                    return paymentRepository.save(payment)
                            .thenMany(ledgerRepository.saveAll(List.of(ledgerPair.credit(), ledgerPair.debit())))
                            .then(accountRepository.update(fromUpdated))
                            .then(accountRepository.update(toUpdated))
                            .thenReturn(payment);
                });

        return logic
                .as(transactionalOperator::transactional)
                .onErrorMap(this::translateConcurrencyError);

    }

    private Throwable translateConcurrencyError(Throwable ex) {
        Throwable root = Exceptions.unwrap(ex);

        if (root instanceof DuplicateKeyException ||
                root instanceof OptimisticLockingFailureException) {

            return new ConcurrentAccountUpdateException();
        }

        return ex;
    }

}
