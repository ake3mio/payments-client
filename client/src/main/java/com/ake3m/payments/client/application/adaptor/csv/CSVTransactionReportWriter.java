package com.ake3m.payments.client.application.adaptor.csv;

import com.ake3m.payments.client.domain.entity.TransactionResultsEntity;
import com.ake3m.payments.client.domain.ports.TransactionReportGeneratorPort;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.vertx.core.Vertx;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class CSVTransactionReportWriter implements TransactionReportGeneratorPort {
    private static final String[] HEADER = new String[]{"ID", "Server-generated ID", "Status", "Fee", "Details"};

    private final Vertx vertx;
    private final TransactionMappingStrategy strategy = new TransactionMappingStrategy();
    private final Path filePath;

    @SneakyThrows
    public CSVTransactionReportWriter(Vertx vertx, Path destination) {
        this(vertx, destination, UUID.randomUUID().toString());
    }

    @SneakyThrows
    public CSVTransactionReportWriter(Vertx vertx, Path destination, String fileName) {
        this.vertx = vertx;
        this.filePath = Path.of(destination.toString(), fileName + ".csv");
        strategy.setType(Transaction.class);
    }

    @Override
    public CompletableFuture<Void> write(TransactionResultsEntity transactionResults) {
        return vertx
                .<Void>executeBlocking(promise -> {
                    try {
                        writeCsv(transactionResults);
                        log.info("The transaction report has been written to {}", filePath);
                        promise.complete();
                    } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException exception) {
                        log.error("Failed to write csv", exception);
                        deleteFile();
                        promise.fail(exception);
                    }
                })
                .toCompletionStage()
                .toCompletableFuture();
    }

    private void deleteFile() {
        if (!filePath.toFile().delete()) {
            log.error("Failed to cleanup failed csv generation. File: {}", filePath);
        }
    }

    private void writeCsv(TransactionResultsEntity transactionResults) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, IOException {
        final var writer = new FileWriter(filePath.toString());
        final var csvWriter = getCSVWriter(writer);
        csvWriter.write(Transaction.mapToDtos(transactionResults.results()));
        writer.close();
    }

    private StatefulBeanToCsv<Transaction> getCSVWriter(FileWriter writer) {
        return new StatefulBeanToCsvBuilder<Transaction>(writer)
                .withSeparator(CSVWriter.DEFAULT_SEPARATOR)
                .withMappingStrategy(strategy)
                .build();
    }

    private static class TransactionMappingStrategy extends ColumnPositionMappingStrategy<Transaction> {
        @Override
        public String[] generateHeader(Transaction bean) throws CsvRequiredFieldEmptyException {
            super.generateHeader(bean);
            return HEADER;
        }
    }
}
