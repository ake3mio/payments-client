package com.ake3m.payments.client.application.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.vertx.core.json.jackson.DatabindCodec;

public final class JsonSerDesConfig {
    public static void configure() {
        DatabindCodec.mapper().enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }
}
