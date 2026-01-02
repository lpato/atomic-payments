package com.lsp.atomic_payments.infra.persistence.payment;

import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentId;
import com.lsp.atomic_payments.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

  private final PaymentR2dbcRepository paymentR2dbcRepository;
  private final PaymentMapper paymentMapper;

  @Override
  public Mono<Payment> findById(PaymentId paymentId) {

    return paymentR2dbcRepository.findById(paymentId.value()).map(paymentMapper::toDomain);
  }

  @Override
  public Mono<Payment> save(Payment payment) {

    return paymentR2dbcRepository
        .save(paymentMapper.toEntity(payment))
        .map(paymentMapper::toDomain);
  }
}
