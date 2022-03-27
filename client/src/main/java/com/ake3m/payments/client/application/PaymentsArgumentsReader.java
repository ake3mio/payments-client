package com.ake3m.payments.client.application;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public record PaymentsArgumentsReader(String[] arguments) {

    public File getFileInput() {
        return getArgument("--input=")
                .map(File::new)
                .filter(File::isFile)
                .orElseThrow(() -> new IllegalArgumentException("input is not a file"));
    }

    public Path getFileOutput() {
        return getArgument("--output=")
                .map(File::new)
                .filter(File::isDirectory)
                .map(file -> Path.of(file.getPath()))
                .orElseThrow(() -> new IllegalArgumentException("output is not a directory"));
    }

    private Optional<String> getArgument(String inputFlag) {
        return Arrays.stream(arguments)
                     .filter(s -> s.startsWith(inputFlag))
                     .map(s -> s.substring(inputFlag.length()))
                     .findFirst();
    }
}
