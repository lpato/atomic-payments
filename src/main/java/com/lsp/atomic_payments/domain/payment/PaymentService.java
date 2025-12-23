package com.lsp.atomic_payments.domain.payment;

import java.util.List;

import org.springframework.transaction.reactive.TransactionalOperator;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountRepository;
import com.lsp.atomic_payments.domain.ledger.EntryType;
import com.lsp.atomic_payments.domain.ledger.LedgerEntry;
import com.lsp.atomic_payments.domain.ledger.LedgerEntryId;
import com.lsp.atomic_payments.domain.ledger.LedgerRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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

                    LedgerEntry entryDebit = new LedgerEntry(
                            LedgerEntryId.newId(),
                            fromAccount.accountId(),
                            payment.paymentId(),
                            payment.amount(),
                            EntryType.DEBIT,
                            payment.createdAt());

                    LedgerEntry entryCredit = new LedgerEntry(
                            LedgerEntryId.newId(),
                            toAccount.accountId(),
                            payment.paymentId(),
                            payment.amount(),
                            EntryType.CREDIT,
                            payment.createdAt());

                    fromAccount.debit(payment.amount());
                    toAccount.credit(payment.amount());

                    return paymentRepository.save(payment)
                            .thenMany(ledgerRepository.saveAll(List.of(entryDebit, entryCredit)))
                            .then(accountRepository.save(fromAccount))
                            .then(accountRepository.save(toAccount))
                            .thenReturn(payment);
                })).single();
    }
}
