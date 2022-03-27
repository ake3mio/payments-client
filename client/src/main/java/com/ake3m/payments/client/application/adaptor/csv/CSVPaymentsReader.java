package com.ake3m.payments.client.application.adaptor.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CSVPaymentsReader {
    private final Vertx vertx;
    private final File file;

    public CSVPaymentsReader(Vertx vertx, File file) {
        this.vertx = vertx;
        this.file = file;
    }

    public Future<List<Payment>> readPayments() {
        return vertx.executeBlocking(promise -> {
            try (var fileReader = getFileReader()) {
                promise.complete(readPayments(fileReader));
            } catch (Exception e) {
                promise.fail(e);
            }
        });
    }

    private List<Payment> readPayments(BufferedReader fileReader) {
        return new CsvToBeanBuilder<Payment>(fileReader)
                .withType(Payment.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build()
                .parse();
    }

    private BufferedReader getFileReader() throws IOException {
        return new BufferedReader(new InputStreamReader(Files.newInputStream(Path.of(file.getPath()))));
    }
}
