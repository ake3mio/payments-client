package com.ake3m.payments.client.functionaltests;

import io.cucumber.datatable.DataTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestUtil {
    private static final String PACKAGE_NAME = TestUtil.class.getPackageName();
    private static final String DIRECTORY = PACKAGE_NAME.replace(".", "/");
    private static final ClassLoader CLASS_LOADER = TestUtil.class.getClassLoader();

    public static String getTestDirectory() {
        return Objects.requireNonNull(CLASS_LOADER.getResource(DIRECTORY)).getPath();
    }

    public static String asResourcePath(Path filePath) {
        return Path.of(DIRECTORY, filePath.toString().split(DIRECTORY)[1]).toString();
    }

    public static Path createInputCsv(DataTable dataTable) throws IOException {
        final var csv = dataTable.asLists()
                                 .stream()
                                 .map(row -> String.join(",", row))
                                 .collect(Collectors.joining("\n"));
        final var filePath = Path.of(getTestDirectory(), UUID.randomUUID() + ".csv");
        return Files.writeString(filePath, csv);
    }

    public static List<String> readCsv(Path directory) {
        try {
            final var file = Objects.requireNonNull(directory.toFile().listFiles())[0];
            return Files.readAllLines(file.toPath());
        } catch (Exception e) {
            return List.of();
        }
    }

    public static Path createOutputDirectory() throws IOException {
        final var filePath = Path.of(getTestDirectory(), UUID.randomUUID().toString());
        return Files.createDirectory(filePath);
    }
}
