package com.lsp.atomic_payments.infra.persistence.payment;

import com.lsp.atomic_payments.domain.account.AccountId;
import com.lsp.atomic_payments.domain.common.Money;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import com.lsp.atomic_payments.domain.payment.PaymentStatus;
import java.util.Currency;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

  public PaymentEntity toEntity(Payment payment) {

    if (payment == null) {
      return null;
    }

    PaymentEntity entity = new PaymentEntity();

    entity.setId(payment.paymentId().value());
    entity.setFromAccountId(payment.fromAccountId().value());
    entity.setToAccountId(payment.toAccountId().value());
    entity.setAmount(payment.amount().amount());
    entity.setCurrency(payment.amount().currency().getCurrencyCode());
    entity.setStatus(payment.status().name().toUpperCase());
    entity.setReference(payment.reference());
    entity.setCreatedAt(payment.createdAt());

    return entity;
  }

  public Payment toDomain(PaymentEntity entity) {

    if (entity == null) {
      return null;
    }

    return new Payment(
        new PaymentId(entity.getId()),
        new AccountId(entity.getFromAccountId()),
        new AccountId(entity.getToAccountId()),
        new Money(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
        PaymentStatus.valueOf(entity.getStatus()),
        entity.getReference(),
        entity.getCreatedAt());
  }
}
