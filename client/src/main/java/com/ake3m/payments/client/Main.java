package com.ake3m.payments.client;

import com.ake3m.payments.client.application.MainVerticle;
import com.ake3m.payments.client.application.PaymentsArgumentsReader;
import com.ake3m.payments.client.application.config.Configuration;
import com.ake3m.payments.client.application.config.JsonSerDesConfig;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

import static com.ake3m.payments.client.application.config.ConfigLoader.loadConfig;

@Slf4j
public class Main {

    public static void main(String[] arguments) {
        JsonSerDesConfig.configure();

        final var paymentsArgumentsReader = new PaymentsArgumentsReader(arguments);
        final var fileInput = paymentsArgumentsReader.getFileInput();
        final var fileOutput = paymentsArgumentsReader.getFileOutput();
        final var vertx = Vertx.vertx();

        loadConfig(vertx)
                .onSuccess(configuration -> deployMainVerticle(fileInput, fileOutput, vertx, configuration))
                .onFailure(throwable -> {
                    log.error("Failed to load configuration", throwable);
                    vertx.close();
                });
    }

    private static void deployMainVerticle(File fileInput, Path fileOutput, Vertx vertx, Configuration configuration) {
        vertx.deployVerticle(new MainVerticle(configuration, fileInput, fileOutput));
    }
}
