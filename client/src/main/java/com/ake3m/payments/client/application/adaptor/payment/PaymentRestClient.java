package com.ake3m.payments.client.application.adaptor.payment;

import com.ake3m.payments.client.application.adaptor.dto.PaymentRequest;
import com.ake3m.payments.client.application.adaptor.dto.PaymentResponse;
import com.ake3m.payments.client.application.adaptor.dto.TokenResponse;
import com.ake3m.payments.client.application.adaptor.dto.TransactionStatusResponse;
import com.ake3m.payments.client.application.adaptor.exception.PaymentResponseException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.ake3m.payments.client.application.adaptor.dto.PaymentStatus.UNKNOWN;

@Slf4j
public class PaymentRestClient {
    private final WebClient client;
    private final PaymentRestClientConfig config;
    private final AtomicReference<String> cachedTokenRef = new AtomicReference<>();

    public PaymentRestClient(Vertx vertx, PaymentRestClientConfig config) {
        client = WebClient.create(
                vertx,
                new WebClientOptions()
                        .setKeepAlive(true)
                        .setDefaultPort(config.clientPort)
                        .setSsl(config.ssl)
                        .setDefaultHost(config.clientHost)
        );
        this.config = config;
    }

    public Future<TokenResponse> getToken() {
        return client
                .post("/auth")
                .putHeader("Api-Key", config.apiKey)
                .sendJsonObject(new JsonObject().put("account", config.apiAccount))
                .map(HttpResponse::bodyAsJsonObject)
                .map(entries -> entries.mapTo(TokenResponse.class));
    }

    public Future<PaymentResponse> pay(PaymentRequest paymentRequest) {
        return withToken(token -> client
                .post("/pay")
                .putHeader("Authorization", "bearer " + token)
                .sendJsonObject(JsonObject.mapFrom(paymentRequest)))
                .flatMap(this::handlePaymentResponse)
                .map(HttpResponse::bodyAsJsonObject)
                .recover(getRecoverFromFailedPaymentHandler(paymentRequest))
                .map(entries -> {
                    entries.put("reference", paymentRequest.getReference());
                    log.info("Raw payment response received: {}", entries);
                    return entries;
                })
                .map(PaymentResponse::mapToDto)
                .onSuccess(paymentResponse -> log.info("Payment response received: {}", paymentResponse))
                .onFailure(throwable -> log.error("Failed to make payment", throwable));

    }

    public Future<TransactionStatusResponse> status(String conversationID) {
        return withToken(token -> client
                .get("/status/" + conversationID)
                .putHeader("Authorization", "bearer " + token)
                .send())
                .map(HttpResponse::bodyAsJsonObject)
                .map(entries -> {
                    log.info("Raw payment status received: {}", entries);
                    return entries;
                })
                .map(TransactionStatusResponse::mapToDto)
                .onSuccess(statusResponse -> log.info("Payment status received: {}", statusResponse))
                .onFailure(throwable -> log.error("Failed to get status", throwable));
    }

    private Future<HttpResponse<Buffer>> withToken(Function<String, Future<HttpResponse<Buffer>>> request) {
        final var cachedToken = this.cachedTokenRef.get();
        if (cachedToken != null) {
            final Future<HttpResponse<Buffer>> apply = request.apply(cachedToken);
            return apply.flatMap(response -> handleWithTokenResponse(request, response));
        }
        return executeWithUpdatedToken(request);
    }

    private Future<HttpResponse<Buffer>> handleWithTokenResponse(Function<String, Future<HttpResponse<Buffer>>> request, HttpResponse<Buffer> response) {
        if (response.statusCode() == 401) {
            return executeWithUpdatedToken(request);
        }

        return Future.succeededFuture(response);
    }

    private Future<HttpResponse<Buffer>> executeWithUpdatedToken(Function<String, Future<HttpResponse<Buffer>>> request) {
        return getToken()
                .map(TokenResponse::getToken)
                .flatMap(token -> {
                    this.cachedTokenRef.set(token);
                    return withToken(request);
                });
    }

    private Function<Throwable, Future<JsonObject>> getRecoverFromFailedPaymentHandler(PaymentRequest paymentRequest) {
        return throwable -> {
            if (throwable instanceof PaymentResponseException) {
                final var paymentResponse = PaymentResponse.builder()
                                                           .status(UNKNOWN)
                                                           .reference(paymentRequest.getReference())
                                                           .message(throwable.getMessage())
                                                           .build();
                return Future.succeededFuture(JsonObject.mapFrom(paymentResponse));
            }

            return Future.failedFuture(throwable);
        };
    }

    private Future<HttpResponse<Buffer>> handlePaymentResponse(HttpResponse<Buffer> response) {
        if (response.statusCode() > 400) {
            return Future.failedFuture(new PaymentResponseException(
                    response.statusCode(),
                    response.bodyAsString()
            ));
        }
        return Future.succeededFuture(response);
    }

    @Getter
    @Builder
    @Jacksonized
    public static class PaymentRestClientConfig {
        private Integer clientPort;
        private String clientHost;
        @Builder.Default
        private Boolean ssl = false;
        private String apiAccount;
        private String apiKey;
    }
}
