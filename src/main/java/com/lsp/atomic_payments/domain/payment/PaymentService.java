package com.lsp.atomic_payments.domain.payment;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerPair;
import com.lsp.atomic_payments.domain.ledger.LedgerRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionalOperator transactionalOperator;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;

    Mono<Payment> initiatePayment(PaymentCommand command) {

        return transactionalOperator.execute(tx -> Mono.zip(
                accountRepository.findById(command.fromAccountId()),
                accountRepository.findById(command.toAccountId())).flatMap(tuple -> {
                    Account fromAccount = tuple.getT1();
                    Account toAccount = tuple.getT2();

                    // validation
                    if (!fromAccount.isActive()) {
                        return Mono.error(new IllegalStateException("From account is not active"));
                    }
                    if (!toAccount.isActive()) {
                        return Mono.error(new IllegalStateException("To account is not active"));
                    }
                    if (!fromAccount.balance().currency().equals(toAccount.balance().currency())) {
                        return Mono.error(new IllegalStateException("Accounts are in different currency"));
                    }
                    if (fromAccount.balance().amount().compareTo(command.amount().amount()) < 0) {
                        return Mono.error(new IllegalStateException("Account has not enough funds"));
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
                })).single();
    }
}
