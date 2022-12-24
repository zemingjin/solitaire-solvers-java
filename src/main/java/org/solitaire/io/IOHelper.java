package org.solitaire.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

public class IOHelper {
    public static String[] loadFile(String path) {
        requireNonNull(path);
        assert !path.isBlank();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            StringBuilder content = new StringBuilder();

            for (String line = reader.readLine(); nonNull(line); line = reader.readLine()) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (!content.isEmpty()) {
                        content.append(" ");
                    }
                    content.append(line.trim());
                }
            }
            return content.toString().split(" ");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
