package io.github.cjstehno.serpent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RequiredArgsConstructor @Slf4j
public class Runner implements Runnable {

    // TODO: refactoring

    private final Venv venv;
    private final Script script;
    private final String[] arguments;

    public Runner(final Venv venv, final Script script) {
        this(venv, script, new String[0]);
    }

    @Override public void run() {
        try {
            val command = new String[]{"bash", "-c", venv.pathFor("bin/python") + " " + script.getPath()};
            val proc = new ProcessBuilder(command).start();

            // Read the output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[Python] {}", line.trim());
                }
            } catch (Exception ex) {
                log.error("Problem with python output stream: {}", ex.getMessage(), ex);
            }

            // Read the errors
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    log.error("[Python] {}", errorLine);
                }
            } catch (Exception ex) {
                log.error("Problem with python error stream: {}", ex.getMessage(), ex);
            }

            val exitCode = proc.waitFor();
            log.info("Finished (exit code {})", exitCode);

        } catch (Exception ex) {
            log.error("Bad things have happened: {}", ex.getMessage(), ex);
        }
    }
}
