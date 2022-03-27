package com.ake3m.payments.client.functionaltests.testcontainer;

import org.testcontainers.containers.Network;

public class TestContainerNetwork {
    private static final Network NETWORK = Network.newNetwork();
    static Network getNetwork() {
        return NETWORK;
    }
}
