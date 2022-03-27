package com.ake3m.payments.client.application.adaptor.exception;

import lombok.Getter;

@Getter
public class PaymentResponseException extends RuntimeException {
    private final int statusCode;

    public PaymentResponseException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
