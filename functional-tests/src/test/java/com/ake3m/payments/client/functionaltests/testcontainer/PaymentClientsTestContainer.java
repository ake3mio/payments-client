package com.ake3m.payments.client.functionaltests.testcontainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class PaymentClientsTestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentClientsTestContainer.class);

    public static GenericContainer<?> create(PaymentClientsTestContainerConfig config) {
        final var applicationConfig = "/testconfig/application-test.yaml";
        return new GenericContainer<>(DockerImageName.parse("ake3m/payments-client:latest"))
                .withClasspathResourceMapping("application-test.yaml", applicationConfig, BindMode.READ_ONLY)
                .withClasspathResourceMapping(config.input.from(), config.input.to(), BindMode.READ_ONLY)
                .withClasspathResourceMapping(config.output.from(), config.output.to(), BindMode.READ_WRITE)
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .withNetworkAliases("payments-client")
                .withNetwork(TestContainerNetwork.getNetwork())
                .withEnv(Map.of(
                        "INPUT", config.input.to(),
                        "OUTPUT", config.output.to(),
                        "CONFIG_FILE", applicationConfig,
                        "VERSION", "1.0.0"
                ));
    }

    public record PaymentClientsTestContainerConfig(VolumeConfig input, VolumeConfig output) {}

    public record VolumeConfig(String from, String to) {}
}
