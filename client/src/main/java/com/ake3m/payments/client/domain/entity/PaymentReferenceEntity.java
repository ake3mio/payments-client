package com.ake3m.payments.client.domain.entity;

public record PaymentReferenceEntity(
        String reference,
        String conversationID,
        String message,
        TransactionResultStatus status) {}
