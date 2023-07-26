package io.github.cjstehno.serpent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

@RequiredArgsConstructor
public class CommandRunner {

    private final String[] commandLine;
    private Path workingDir;
    private LineHandler outputHandler, errorHandler;


    public static CommandRunner forCommand(final String cmd) {
        return new CommandRunner(new String[]{"bash", "-c", cmd});
    }

    public CommandRunner workingDir(final Path wd) {
        workingDir = wd;
        return this;
    }

    public CommandRunner outputHandler(final LineHandler handler) {
        outputHandler = handler;
        return this;
    }

    public CommandRunner errorHandler(final LineHandler handler) {
        errorHandler = handler;
        return this;
    }

    public int execute() throws IOException, InterruptedException {
        val builder = new ProcessBuilder(commandLine);

        if (workingDir != null) {
            builder.directory(workingDir.toFile());
        }

        val proc = builder.start();

        // Read the output
        try (val reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputHandler.handleLine(line);
            }
        }

        // Read the errors
        try (val errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorHandler.handleLine(errorLine);
            }
        }

        return proc.waitFor();
    }


    public interface LineHandler {

        void handleLine(final String line);
    }

    @RequiredArgsConstructor @Slf4j
    public static final class LoggingLineHandler implements LineHandler {
        private final boolean error;

        @Override public void handleLine(final String line) {
            if (error) {
                log.error("[Python] {}", line.trim());
            } else {
                log.info("[Python] {}", line.trim());
            }
        }
    }
}
