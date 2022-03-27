package com.ake3m.payments.client.functionaltests.steps;

import com.ake3m.payments.client.functionaltests.TestUtil;
import com.ake3m.payments.client.functionaltests.testcontainer.WiremockTestContainer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ake3m.payments.client.functionaltests.testcontainer.PaymentClientsTestContainer.create;
import static com.ake3m.payments.client.functionaltests.testcontainer.PaymentClientsTestContainer.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class StepDefs {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final GenericContainer<?> providerContainer = WiremockTestContainer.create();
    private static final String API_SECRET = "apiSecretAbc123";
    private static final String PAYMENT_DATE = "1970-01-01T02:30:00.000+00:00";
    private static final int SUCCESS_STATUS = 0;
    private static final int FAILED_REQUEST_STATUS = 1000;

    private final ScenarioContext scenarioContext;

    @Inject
    public StepDefs(ScenarioContext scenarioContext) {
        this.scenarioContext = scenarioContext;
    }

    @Before
    public void before() throws JsonProcessingException {
        providerContainer.start();
        final var address = providerContainer.getHost();
        final var port = providerContainer.getFirstMappedPort();
        configureFor(address, port);
        reset();
        setupAuthTokenMock();
    }

    @Given("the user has a set of recipients to pay")
    public void theUserHasASetOfRecipientsToPay(DataTable dataTable) throws IOException {
        final var input = new VolumeConfig(
                TestUtil.asResourcePath(TestUtil.createInputCsv(dataTable)),
                "/testsources/input/input.csv"
        );
        final var outputDirectory = TestUtil.createOutputDirectory();
        final var output = new VolumeConfig(
                TestUtil.asResourcePath(outputDirectory),
                "/testsources/output"
        );

        scenarioContext.setContainer(create(new PaymentClientsTestContainerConfig(input, output)));
        scenarioContext.setOutputDirectory(outputDirectory);
        scenarioContext.setPayments(dataTable.asLists().stream().skip(1).toList());
    }

    @When("the payments are submitted to the client")
    public void thePaymentsAreSubmittedToTheClient() {
        final var payments = scenarioContext.getPayments();

        payments.forEach(row -> {
            final var id = row.get(SUCCESS_STATUS);
            final var recipient = row.get(1);
            final var amount = row.get(2);
            final var currency = row.get(3);
            setupPayMock(id, recipient, amount, currency);
        });

        scenarioContext.getContainer().start();
    }

    @Then("^a report is generated showing all (.+) transactions$")
    public void aReportIsGeneratedShowingAllTransactions(String type) throws JsonProcessingException {
        final var status = getStatus(type);
        final var stringStatus = getStringStatus(status);
        final var message = stringStatus.toLowerCase();
        final var fees = setupPaymentStatusMocks(status, message);

        await()
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    final var csv = TestUtil.readCsv(scenarioContext.getOutputDirectory());
                    assertThat(csv.isEmpty()).isFalse();
                    assertThat(csv.get(0)).isEqualTo("\"ID\",\"Server-generated ID\",\"Status\",\"Fee\",\"Details\"");
                    scenarioContext.getPayments().forEach(row -> {
                        final var id = row.get(0);
                        assertThat(containsRow(csv, id, fees.get(id), status, message)).isTrue();
                    });
                });
    }

    private String getStringStatus(int status) {
        return status == SUCCESS_STATUS ? "Success" : "Failure";
    }

    private int getStatus(String type) {
        return Objects.equals(type, "succeeded") ? SUCCESS_STATUS : FAILED_REQUEST_STATUS;
    }

    private HashMap<String, String> setupPaymentStatusMocks(int status, String message) throws JsonProcessingException {
        final var payments = scenarioContext.getPayments();
        final var fees = new HashMap<String, String>();
        for (int i = 0; i < payments.size(); i++) {
            var row = payments.get(i);
            final var id = row.get(0);
            final var fee = String.valueOf(i);
            fees.put(id, fee);
            setupStatusMock(id, fee, message, status);
        }
        return fees;
    }

    private boolean containsRow(List<String> csv, String reference, String fee, int status, String details) {
        final var expected = createCsvRow(reference, fee, status, details);
        return csv.stream().anyMatch(expected::equals);
    }

    private String createCsvRow(String reference, String fee, int status, String details) {
        final var isSuccessStatus = status == SUCCESS_STATUS;
        return Stream.of(reference,
                         createServerReference(reference),
                         getStringStatus(status),
                         isSuccessStatus ? fee : null,
                         details)
                     .map(this::addQuotes)
                     .collect(Collectors.joining(","));
    }

    private void setupAuthTokenMock() throws JsonProcessingException {
        final var body = OBJECT_MAPPER.writeValueAsString(Map.of("account", "functional-tests-payments-client"));
        final var response = OBJECT_MAPPER.writeValueAsString(Map.of("token", API_SECRET));

        stubFor(post("/auth")
                        .withHeader("Api-Key", equalTo("functional-tests-apikey"))
                        .withRequestBody(equalToJson(body))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                               .withBody(response)));
    }

    private void setupPayMock(
            String reference,
            String recipient,
            String amount,
            String currency) {
        try {
            final var body = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "recipient", recipient,
                    "amount", amount,
                    "currency", currency,
                    "reference", reference
            ));
            final var response = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "conversationID", createConversationId(reference),
                    "message", "",
                    "status", 100
            ));
            stubFor(post("/pay")
                            .withHeader("Authorization", equalTo("bearer " + API_SECRET))
                            .withRequestBody(equalToJson(body))
                            .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                                   .withBody(response)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void setupStatusMock(
            String reference,
            String fee,
            String message,
            int status) throws JsonProcessingException {

        final var response = OBJECT_MAPPER.writeValueAsString(Map.of(
                "customerReference", reference,
                "reference", createServerReference(reference),
                "message", message,
                "fee", fee,
                "status", status,
                "timestamp", StepDefs.PAYMENT_DATE
        ));

        stubFor(get("/status/" + createConversationId(reference))
                        .withHeader("Authorization", equalTo("bearer " + API_SECRET))
                        .willReturn(aResponse().withHeader("Content-Type", "application/json")
                                               .withBody(response)));
    }

    private String addQuotes(String value) {
        return value == null ? "\"" + "\"" : "\"" + value + "\"";
    }

    private String createConversationId(String reference) {
        return reference + "-conversation-id";
    }

    private String createServerReference(String reference) {
        return reference + "-server-reference";
    }
}
