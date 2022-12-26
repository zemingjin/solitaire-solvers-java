package org.solitaire.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class IOHelper {
    private static final String SINGLE = " ";

    public static String[] loadFile(String path) {
        requireNonNull(path);
        assert !path.isBlank();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            StringBuilder content = new StringBuilder();

            for (String line = reader.readLine(); nonNull(line); line = reader.readLine()) {
                if (!line.isBlank()) {
                    if (!content.isEmpty()) {
                        content.append(SINGLE);
                    }
                    content.append(line);
                }
            }
            return toArray(content.toString());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String[] toArray(String line) {
        return Stream.of(line.split(SINGLE))
                .map(String::trim)
                .filter(it -> !it.isEmpty())
                .toArray(String[]::new);
    }
}
