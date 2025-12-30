package com.lsp.atomic_payments.infra.persistence.idempotency;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;

import com.lsp.atomic_payments.domain.account.Account;
import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.account.AccountStatus;
import com.lsp.atomic_payments.domain.account.AccountVersion;
import com.lsp.atomic_payments.domain.common.Idempotency;
import com.lsp.atomic_payments.domain.common.IdempotencyUtils;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;

import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(IdempotencyRecordRepositoryImpl.class)
class IdempotencyRecordRepositoryImplTest {

        @Autowired
        IdempotencyRecordRepositoryImpl idempotencyRepository;
        IdempotencyUtils utils;

        Account from;
        Account to;
        String key;
        private static final AccountVersion VERSION = new AccountVersion(0);
        private static final Instant NOW = Instant.now();
        private static final String PAYLOAD = "PAYLOAD";

        @BeforeEach
        void setUp() {

                key = UUID.randomUUID().toString();

                utils = new IdempotencyUtils();

                from = new Account(
                                AccountId.newId(),
                                "idempotencyFrom",
                                new Money(BigDecimal.valueOf(10), Currency.getInstance("EUR")),
                                AccountStatus.ACTIVE,
                                VERSION,
                                NOW);

                to = new Account(
                                AccountId.newId(),
                                "idempotencyTo",
                                new Money(BigDecimal.valueOf(10), Currency.getInstance("EUR")),
                                AccountStatus.ACTIVE,
                                VERSION,
                                NOW);

        }

        @Test
        void testSaveAndFindByKey() {

                PaymentCommand command = new PaymentCommand(
                                from.accountId(),
                                to.accountId(),
                                new Money(BigDecimal.valueOf(5), Currency.getInstance("EUR")),
                                "REFERENCE",
                                key);

                Idempotency record = new Idempotency(
                                key,
                                utils.hash(command),
                                PAYLOAD,
                                Instant.now());

                StepVerifier.create(idempotencyRepository.save(record).then(idempotencyRepository.findByKey(key)))
                                .assertNext(found -> {
                                        assertThat(found.key()).isEqualTo(key);
                                        assertThat(found.requestHash()).isEqualTo(utils.hash(command));
                                        assertThat(found.responsePayload()).isEqualTo(PAYLOAD);
                                        assertThat(found.createdAt()).isEqualTo(record.createdAt());

                                });

        }
}
