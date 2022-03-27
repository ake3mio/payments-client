package com.ake3m.payments.client.application.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ConfigLoader {
    public static final String PROFILE = System.getProperty("profile", "local");
    public static final String OVERRIDE_CONFIG_FILE_PATH = System.getProperty("config.file", "application-" + PROFILE + ".yaml");

    public static Future<Configuration> loadConfig(Vertx vertx) {
        return getConfigRetriever(vertx).getConfig()
                                        .map(entries -> entries.mapTo(Configuration.class));
    }

    private static ConfigRetriever getConfigRetriever(Vertx vertx) {
        return ConfigRetriever.create(vertx, getRetrieverOptions());
    }

    private static ConfigRetrieverOptions getRetrieverOptions() {
        return new ConfigRetrieverOptions().addStore(getDefaultStore())
                                           .addStore(getOverrideStore());
    }

    private static ConfigStoreOptions getOverrideStore() {
        return new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", OVERRIDE_CONFIG_FILE_PATH));
    }

    private static ConfigStoreOptions getDefaultStore() {
        return new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "application.yaml"));
    }
}
