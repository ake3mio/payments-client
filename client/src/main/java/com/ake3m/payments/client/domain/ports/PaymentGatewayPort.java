package com.ake3m.payments.client.domain.ports;

import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.PaymentRequestEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PaymentGatewayPort {
    CompletableFuture<List<PaymentReferenceEntity>> pay(List<PaymentRequestEntity> payments);
}
