package com.ake3m.payments.client.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionResultStatus {
    FAILURE("Failure"),
    SUCCESS("Success"),
    UNKNOWN("Unknown");

    private final String value;
}
