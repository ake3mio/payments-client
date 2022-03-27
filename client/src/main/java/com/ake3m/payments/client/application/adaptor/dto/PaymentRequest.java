package com.ake3m.payments.client.application.adaptor.dto;

import com.ake3m.payments.client.domain.entity.PaymentRequestEntity;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@EqualsAndHashCode
public class PaymentRequest {
    private String recipient;
    private String amount;
    private String currency;
    private String reference;

    public static PaymentRequest mapToDto(PaymentRequestEntity entity) {
        return PaymentRequest
                .builder()
                .recipient(entity.recipient())
                .currency(entity.currency())
                .amount(entity.amount())
                .reference(entity.reference())
                .build();
    }
}
