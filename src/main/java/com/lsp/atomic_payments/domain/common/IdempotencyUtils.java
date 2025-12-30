package com.lsp.atomic_payments.domain.common;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsp.atomic_payments.domain.payment.Payment;
import com.lsp.atomic_payments.domain.payment.PaymentCommand;

public class IdempotencyUtils {

    private final ObjectMapper mapper = new ObjectMapper();

    public String serialize(Payment payment) {
        try {
            return mapper.writeValueAsString(payment);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize payment", e);
        }
    }

    public Payment deserialize(String payload) {

        try {
            return mapper.readValue(payload, Payment.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize payment", e);
        }

    }

    public String hash(PaymentCommand command) {

        return DigestUtils.sha256Hex(
                command.canonical().getBytes(StandardCharsets.UTF_8));

    }

}
