package com.ake3m.payments.client.application.adaptor.dto;

import com.ake3m.payments.client.domain.entity.TransactionResultStatus;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    TRANSACTION_SUCCEEDED(0),
    TRANSACTION_PENDING(100),
    INVALID_REQUEST(1000),
    RECIPIENT_PHONE_NUMBER_WAS_NOT_VALID(20000),
    RECIPIENT_ACCOUNT_IS_LOCKED(20001),
    SENDER_ACCOUNT_IS_LOCKED(20002),
    RECIPIENT_WALLET_IS_FULL(20003),
    INSUFFICIENT_BALANCE_IN_SENDER_ACCOUNT(20004),
    TEMPORARY_FAILURE(20005),
    RECIPIENT_PHONE_NUMBER_IS_NOT_REGISTERED_FOR_MOBILE_MONEY(20014),
    DUPLICATE_REFERENCE(30006),
    @JsonEnumDefaultValue
    UNKNOWN(-1);

    static final List<PaymentStatus> FAILURE_STATUSES = List.of(INVALID_REQUEST,
                                                                RECIPIENT_PHONE_NUMBER_WAS_NOT_VALID,
                                                                RECIPIENT_ACCOUNT_IS_LOCKED,
                                                                SENDER_ACCOUNT_IS_LOCKED,
                                                                RECIPIENT_WALLET_IS_FULL,
                                                                INSUFFICIENT_BALANCE_IN_SENDER_ACCOUNT,
                                                                TEMPORARY_FAILURE,
                                                                RECIPIENT_PHONE_NUMBER_IS_NOT_REGISTERED_FOR_MOBILE_MONEY,
                                                                DUPLICATE_REFERENCE);

    static final List<PaymentStatus> SUCCESS_STATUSES = List.of(TRANSACTION_SUCCEEDED);

    private final Integer status;

    public static PaymentStatus findByStatus(Integer value) {
        return Arrays.stream(PaymentStatus.values())
                     .filter(paymentStatus -> Objects.equals(paymentStatus.status, value))
                     .findFirst()
                     .orElse(null);
    }

    public static TransactionResultStatus mapToEntity(PaymentStatus status) {
        if (FAILURE_STATUSES.contains(status))
            return TransactionResultStatus.FAILURE;

        if (SUCCESS_STATUSES.contains(status))
            return TransactionResultStatus.SUCCESS;

        return TransactionResultStatus.UNKNOWN;
    }
}
