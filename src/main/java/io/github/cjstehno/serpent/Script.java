package io.github.cjstehno.serpent;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.writeString;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Script {

    // Allows resolving a script as a File path from various sources

    @Getter private final Path path;

    // FIXME: move out
    public static class FileScript extends Script {

        public FileScript(final Path filePath) {
            super(filePath);
        }
    }

    public static class TextScript extends Script {
        // TODO: an example of how another impl might look

        public TextScript(final String text) throws IOException {
            super(createTempFile("script-", ".py"));

            // write the content to the temporary file
            writeString(getPath(), text);
        }
    }
}
