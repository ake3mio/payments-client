package com.ake3m.payments.client.application.adaptor.payment;

import com.ake3m.payments.client.domain.entity.PaymentReferenceEntity;
import com.ake3m.payments.client.domain.entity.PaymentRequestEntity;
import com.ake3m.payments.client.domain.ports.PaymentGatewayPort;
import com.ake3m.payments.client.application.adaptor.dto.PaymentRequest;
import com.ake3m.payments.client.application.adaptor.dto.PaymentResponse;
import com.ake3m.payments.client.application.adaptor.dto.PaymentStatus;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RequiredArgsConstructor
public class PaymentGateway implements PaymentGatewayPort {

    private final PaymentRestClient client;

    @Override
    public CompletableFuture<List<PaymentReferenceEntity>> pay(List<PaymentRequestEntity> payments) {
        return CompositeFuture.join(makePayments(payments))
                              .map(CompositeFuture::list)
                              .map(this::mapToPaymentReferenceEntity)
                              .toCompletionStage()
                              .toCompletableFuture();
    }

    private List<Future> makePayments(List<PaymentRequestEntity> payments) {
        return payments.stream()
                       .map(this::pay)
                       .map(Future.class::cast)
                       .toList();
    }

    private List<PaymentReferenceEntity> mapToPaymentReferenceEntity(List<?> objects) {
        return objects.stream()
                      .map(PaymentResponse.class::cast)
                      .map(PaymentResponse::mapToEntity)
                      .toList();
    }

    private Future<PaymentResponse> pay(PaymentRequestEntity payment) {
        final var paymentRequest = PaymentRequest.mapToDto(payment);
        return client.pay(paymentRequest)
                     .map(mapPaymentResponse(paymentRequest))
                     .recover(recoverFromPaymentRequest(paymentRequest));
    }

    private Function<Throwable, Future<PaymentResponse>> recoverFromPaymentRequest(PaymentRequest paymentRequest) {
        return throwable -> {
            final var response = PaymentResponse.builder()
                                                .reference(paymentRequest.getReference())
                                                .message(throwable.getMessage())
                                                .status(PaymentStatus.INVALID_REQUEST)
                                                .build();
            return Future.succeededFuture(response);
        };
    }

    private Function<PaymentResponse, PaymentResponse> mapPaymentResponse(PaymentRequest paymentRequest) {
        return paymentResponse -> PaymentResponse.builder()
                                                 .reference(paymentRequest.getReference())
                                                 .conversationID(paymentResponse.getConversationID())
                                                 .message(paymentResponse.getMessage())
                                                 .status(paymentResponse.getStatus())
                                                 .build();
    }
}
