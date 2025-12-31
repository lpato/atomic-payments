package com.lsp.atomic_payments.application.payment;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentEventListener {

    @EventListener
    public void onPaymentInitiated(PaymentInitiatedEvent event) {
        log.info("Payment initiated: {}", event.paymentId());
        // future: publish to Kafka / SNS / etc
    }
}
