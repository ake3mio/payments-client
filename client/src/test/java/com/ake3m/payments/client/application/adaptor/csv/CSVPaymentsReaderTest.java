package com.ake3m.payments.client.application.adaptor.csv;

import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(VertxExtension.class)
class CSVPaymentsReaderTest {
    private final ClassLoader classLoader = getClass().getClassLoader();
    private final URL classLoaderResource = classLoader.getResource(".");
    private final String rootPath = Objects.requireNonNull(classLoaderResource).getPath();

    @Test
    void readPayments(Vertx vertx) {
        final var filePath = Path.of(rootPath, "test-input.csv");
        final var csvPaymentsReader = new CSVPaymentsReader(vertx, filePath.toFile());
        final var payments = csvPaymentsReader
                .readPayments()
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        assertThat(payments).containsExactly(
                new Payment("id1", "recipient1", "10", "KES"),
                new Payment("id2", "recipient2", "50", "USD"),
                new Payment("id3", "recipient3", "150.35", "USD"),
                new Payment("id4", "recipient4", "37", "NGN")
        );
    }

    @ParameterizedTest
    @MethodSource("missingFieldCSVs")
    void readPaymentsMissingField(String csv) {
        final var vertx = Vertx.vertx();
        final var filePath = Path.of(rootPath, csv);
        final var csvPaymentsReader = new CSVPaymentsReader(vertx, filePath.toFile());

        assertThatThrownBy(() -> csvPaymentsReader
                .readPayments()
                .toCompletionStage()
                .toCompletableFuture()
                .join()).getCause().hasCauseInstanceOf(CsvRequiredFieldEmptyException.class);
    }

    private static Stream<Arguments> missingFieldCSVs() {
        return Stream.of(
                Arguments.of("test-input-missing-amount.csv"),
                Arguments.of("test-input-missing-currency.csv"),
                Arguments.of("test-input-missing-id.csv"),
                Arguments.of("test-input-missing-recipient.csv")
        );
    }
}
