package com.ake3m.payments.client.application.adaptor;

import com.ake3m.payments.client.application.adaptor.dto.*;
import com.ake3m.payments.client.application.adaptor.payment.PaymentRestClient;
import com.ake3m.payments.client.application.config.JsonSerDesConfig;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static com.ake3m.payments.client.application.adaptor.payment.PaymentRestClient.PaymentRestClientConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(VertxExtension.class)
class PaymentRestClientTest {

    public static final String EXPECTED_TOKEN = "abc123";
    private static final String API_KEY = "my api key";
    public static final String API_ACCOUNT = "apiapplication12334";

    @Container
    public GenericContainer<?> providerContainer = new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:2.32.0-alpine"))
            .withExposedPorts(8080);

    private PaymentRestClient client;

    @BeforeEach
    public void setUp(Vertx vertx) {
        JsonSerDesConfig.configure();

        final var address = providerContainer.getHost();
        final var port = providerContainer.getFirstMappedPort();
        final var config = PaymentRestClientConfig.builder()
                                                  .apiKey(API_KEY)
                                                  .apiAccount(API_ACCOUNT)
                                                  .clientHost(address)
                                                  .clientPort(port)
                                                  .build();
        configureFor(address, port);
        client = new PaymentRestClient(vertx, config);
    }

    @Test
    void getToken(VertxTestContext testContext) throws InterruptedException {

        setupAuthTokenMock();

        client.getToken()
              .onComplete(testContext.succeeding(tokenResponse -> {
                  assertThat(tokenResponse.getToken()).isEqualTo(EXPECTED_TOKEN);
                  testContext.completeNow();
              }));

        assertThat(testContext.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
        assertThat(testContext.completed()).isTrue();
    }

    private void setupAuthTokenMock() {
        final var expectedBody = new JsonObject().put("account", API_ACCOUNT).encode();
        final var response = TokenResponse.builder().token(EXPECTED_TOKEN).build();
        stubFor(post("/auth")
                        .withHeader("Api-Key", equalTo(API_KEY))
                        .withRequestBody(equalTo(expectedBody))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                               .withBody(JsonObject.mapFrom(response).encode())));
    }

    @Test
    void pay(VertxTestContext testContext) throws InterruptedException {
        final var paymentRequest = PaymentRequest.builder()
                                                 .recipient("254999999999")
                                                 .amount("1000.23")
                                                 .currency("KES")
                                                 .build();
        final var expectedResponse = PaymentResponse.builder()
                                                    .conversationID("254999999999")
                                                    .message("abc")
                                                    .status(PaymentStatus.TRANSACTION_SUCCEEDED)
                                                    .build();
        setupAuthTokenMock();

        stubFor(post("/pay")
                        .withHeader("Authorization", equalTo("bearer " + EXPECTED_TOKEN))
                        .withRequestBody(equalTo(JsonObject.mapFrom(paymentRequest).encode()))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                               .withBody(JsonObject.mapFrom(expectedResponse).put("status", 0).encode())));
        client.pay(paymentRequest)
              .onComplete(testContext.succeeding(response -> {
                  assertThat(response.getMessage()).isEqualTo(expectedResponse.getMessage());
                  assertThat(response.getStatus()).isEqualTo(expectedResponse.getStatus());
                  assertThat(response.getConversationID()).isEqualTo(expectedResponse.getConversationID());
                  testContext.completeNow();
              }));

        assertThat(testContext.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
        assertThat(testContext.completed()).isTrue();
    }

    @Test
    void payWithUnknownStatus(VertxTestContext testContext) throws InterruptedException {

        final var paymentRequest = PaymentRequest.builder()
                                                 .recipient("254999999999")
                                                 .amount("1000.23")
                                                 .currency("KES")
                                                 .build();
        final var expectedResponse = PaymentResponse.builder()
                                                    .conversationID("254999999999")
                                                    .message("abc")
                                                    .build();

        setupAuthTokenMock();

        stubFor(post("/pay")
                        .withHeader("Authorization", equalTo("bearer " + EXPECTED_TOKEN))
                        .withRequestBody(equalTo(JsonObject.mapFrom(paymentRequest).encode()))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                               .withBody(JsonObject.mapFrom(expectedResponse).put("status", 234234553).encode())));
        client.pay(paymentRequest)
              .onComplete(testContext.succeeding(response -> {
                  assertThat(response.getMessage()).isEqualTo(expectedResponse.getMessage());
                  assertThat(response.getStatus()).isEqualTo(null);
                  assertThat(response.getConversationID()).isEqualTo(expectedResponse.getConversationID());
                  testContext.completeNow();
              }));

        assertThat(testContext.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
        assertThat(testContext.completed()).isTrue();
    }

    @Test
    void status(VertxTestContext testContext) throws InterruptedException {
        final var conversationID = "my-conversation-id";
        final var date = "1970-01-01T02:30:00.000+00:00";
        final var expectedResponse = TransactionStatusResponse.builder()
                                                              .timestamp(Instant.parse(date))
                                                              .reference("my reference")
                                                              .message("status message")
                                                              .customerReference("customer reference")
                                                              .status(PaymentStatus.TRANSACTION_SUCCEEDED)
                                                              .fee(BigDecimal.TEN)
                                                              .build();

        setupAuthTokenMock();

        final var body = JsonObject
                .mapFrom(expectedResponse)
                .put("timestamp", date)
                .put("status", PaymentStatus.TRANSACTION_SUCCEEDED.getStatus())
                .encode();

        stubFor(get("/status/" + conversationID)
                        .withHeader("Authorization", equalTo("bearer " + EXPECTED_TOKEN))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                               .withBody(body)));
        client.status(conversationID)
              .onComplete(testContext.succeeding(response -> {
                  assertThat(response.getTimestamp()).isEqualTo(expectedResponse.getTimestamp());
                  assertThat(response.getReference()).isEqualTo(expectedResponse.getReference());
                  assertThat(response.getMessage()).isEqualTo(expectedResponse.getMessage());
                  assertThat(response.getCustomerReference()).isEqualTo(expectedResponse.getCustomerReference());
                  assertThat(response.getStatus()).isEqualTo(expectedResponse.getStatus());
                  assertThat(response.getFee()).isEqualTo(expectedResponse.getFee());
                  testContext.completeNow();
              }));

        assertThat(testContext.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
        assertThat(testContext.completed()).isTrue();
    }
}
