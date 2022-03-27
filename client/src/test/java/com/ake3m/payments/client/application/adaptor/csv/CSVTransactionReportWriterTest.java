package com.ake3m.payments.client.application.adaptor.csv;

import com.ake3m.payments.client.domain.entity.TransactionResultEntity;
import com.ake3m.payments.client.domain.entity.TransactionResultStatus;
import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.ake3m.payments.TestData.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class CSVTransactionReportWriterTest {
    private final ClassLoader classLoader = getClass().getClassLoader();
    private final URL classLoaderResource = classLoader.getResource(".");
    private final Path rootPath = Path.of(Objects.requireNonNull(classLoaderResource).getPath());

    @Test
    void generate(Vertx vertx) throws IOException {
        final var fileName = UUID.randomUUID().toString();
        final var file = Path.of(rootPath.toString(), fileName + ".csv");
        final var csvTransactionReportGenerator = new CSVTransactionReportWriter(vertx, rootPath, fileName);

        csvTransactionReportGenerator.write(new TransactionResultsEntity(List.of(
                TRANSACTION_RESULT_ENTITY,
                TRANSACTION_RESULT_ENTITY_TWO,
                TRANSACTION_RESULT_ENTITY_THREE
        ))).join();

        final var transactions = read(file);

        assertThat(transactions).isEqualTo(List.of(
                Transaction.mapToDto(TRANSACTION_RESULT_ENTITY),
                Transaction.mapToDto(TRANSACTION_RESULT_ENTITY_TWO),
                Transaction.mapToDto(TRANSACTION_RESULT_ENTITY_THREE)
        ));
    }

    @Test
    void generateWithMissingRequiredId(Vertx vertx) {
        final var fileName = UUID.randomUUID().toString();
        final var csvTransactionReportGenerator = new CSVTransactionReportWriter(vertx, rootPath, fileName);
        final var filePath = Path.of(rootPath.toString(), fileName + ".csv");
        final var transactionResults = new TransactionResultsEntity(List.of(
                new TransactionResultEntity(
                        null,
                        "server id 1",
                        TransactionResultStatus.SUCCESS,
                        BigDecimal.ONE,
                        "details 1"
                )
        ));
        assertThatThrownBy(() -> csvTransactionReportGenerator.write(transactionResults).join())
                .hasCauseInstanceOf(CsvRequiredFieldEmptyException.class);

        assertThat(filePath.toFile().isFile()).isFalse();
    }

    @Test
    void generateWithMissingRequiredTransactionResultStatus(Vertx vertx) {
        final var fileName = UUID.randomUUID().toString();
        final var csvTransactionReportGenerator = new CSVTransactionReportWriter(vertx, rootPath, fileName);
        final var filePath = Path.of(rootPath.toString(), fileName + ".csv");
        final var transactionResults = new TransactionResultsEntity(List.of(
                new TransactionResultEntity(
                        "id",
                        "server id 1",
                        null,
                        BigDecimal.ONE,
                        "details 1"
                )
        ));
        assertThatThrownBy(() -> csvTransactionReportGenerator.write(transactionResults).join())
                .hasCauseInstanceOf(CsvRequiredFieldEmptyException.class);
        assertThat(filePath.toFile().isFile()).isFalse();
    }

    @Test
    void generateWithMissingNonRequiredFields(Vertx vertx) {
        final var fileName = UUID.randomUUID().toString();
        final var csvTransactionReportGenerator = new CSVTransactionReportWriter(vertx, rootPath, fileName);
        final var filePath = Path.of(rootPath.toString(), fileName + ".csv");
        final var transactionResults = new TransactionResultsEntity(List.of(
                new TransactionResultEntity(
                        "id",
                        null,
                        TransactionResultStatus.FAILURE,
                        null,
                        null
                )
        ));

        assertThatNoException().isThrownBy(() -> csvTransactionReportGenerator.write(transactionResults).join());
        assertThat(filePath.toFile().isFile()).isTrue();
    }

    private List<Transaction> read(Path filePath) throws IOException {
        try (var fileReader = new BufferedReader(new InputStreamReader(Files.newInputStream(filePath)))) {
            return new CsvToBeanBuilder<Transaction>(fileReader)
                    .withType(Transaction.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(1)
                    .build()
                    .parse();
        }
    }
}
