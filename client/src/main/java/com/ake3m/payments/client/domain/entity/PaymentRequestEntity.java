package com.ake3m.payments.client.domain.entity;

public record PaymentRequestEntity(
        String recipient,
        String amount,
        String currency,
        String reference) {}
