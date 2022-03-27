package com.ake3m.payments;

import com.ake3m.payments.client.domain.entity.PaymentRequestEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultStatus;

import java.math.BigDecimal;

public class TestData {
    public static final PaymentRequestEntity PAYMENT_REQUEST = new PaymentRequestEntity(
            "254999999999",
            "1000.23",
            "KES",
            "a"
    );

    public static final PaymentRequestEntity PAYMENT_REQUEST_TWO = new PaymentRequestEntity(
            "2549999999993",
            "1003.23",
            "GBP",
            "b"
    );

    public static final TransactionResultEntity TRANSACTION_RESULT_ENTITY = new TransactionResultEntity(
            PAYMENT_REQUEST.recipient(),
            "server id 1",
            TransactionResultStatus.SUCCESS,
            BigDecimal.ONE,
            "details 1"
    );

    public static final TransactionResultEntity TRANSACTION_RESULT_ENTITY_TWO = new TransactionResultEntity(
            PAYMENT_REQUEST_TWO.recipient(),
            "server id 2",
            TransactionResultStatus.SUCCESS,
            new BigDecimal("4.34"),
            "details 2"
    );

    public static final TransactionResultEntity TRANSACTION_RESULT_ENTITY_THREE = new TransactionResultEntity(
            PAYMENT_REQUEST_TWO.recipient(),
            null,
            TransactionResultStatus.FAILURE,
            null,
            "details 3"
    );

}
