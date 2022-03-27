package com.ake3m.payments.client.functionaltests.testcontainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

public class WiremockTestContainer {
    private static final Logger LOG = LoggerFactory.getLogger(WiremockTestContainer.class);

    public static GenericContainer<?> create() {
        return new GenericContainer<>(DockerImageName.parse("wiremock/wiremock:2.32.0-alpine"))
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .withNetworkAliases("payments-provider")
                .withNetwork(TestContainerNetwork.getNetwork())
                .withExposedPorts(8080);
    }
}
