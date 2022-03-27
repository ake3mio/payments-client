package com.ake3m.payments.client.functionaltests.steps;

import io.cucumber.guice.ScenarioScoped;
import org.testcontainers.containers.GenericContainer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

@ScenarioScoped
public class ScenarioContext {
    private GenericContainer<?> container;
    private Path outputDirectory;
    private List<List<String>> payments;

    public GenericContainer<?> getContainer() {
        return container;
    }

    public void setContainer(GenericContainer<?> container) {
        this.container = container;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setPayments(List<List<String>> payments) {
        this.payments = payments;
    }

    public List<List<String>> getPayments() {
        return payments;
    }
}
