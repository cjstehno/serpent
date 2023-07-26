package io.github.cjstehno.serpent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor @Slf4j
public class Venv {

    private final Path path;
    private final String version;

    /*
        FIXME:
        - select existing VENV to use
        - create one from pyenv + pip + requirements

        - would be nice to instlal a selected version into pyenv
        - list installed modules?

        - needs refactoring
     */

    public Path path() {
        return path;
    }

    public String version() {
        return version;
    }

    public Path pathFor(final String childPath) {
        return path.resolve(childPath);
    }

    public Path pathFor(final Path childPath) {
        return path.resolve(childPath);
    }

    public static boolean venvExists(final Path venvPath) {
        val dirExists = Files.exists(venvPath);
        val isDir = Files.isDirectory(venvPath);
        val pythonExists = Files.exists(venvPath.resolve("bin/python"));
        return dirExists && isDir && pythonExists;
    }

    public static Venv useVenv(final Path venvPath) throws IOException, InterruptedException {
        val command = new String[]{"bash", "-c", venvPath.resolve("bin/python") + " --version"};
        val proc = new ProcessBuilder(command).start();

        String version = "";

        // Read the output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                version = line.split(" ")[1];
            }
        }

        // Read the errors
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("[Python] {}", errorLine);
            }
        }

        proc.waitFor();

        return new Venv(venvPath, version);
    }

    // FIXME: pyenv must be installed and accessible (with desired version installed)
    // NOTE: this operation takes some time
    public static Venv createVenv(final Path workingDir, final String pyVersion, final Path venvPath) throws IOException, InterruptedException {
        // FIXME: pyenv install version
        // FIXME: use pyenv as version to create venv
        // FIXME install
        // pyenv shell 3.9.5 && python -m venv /path/to/your/venv

        val command = new String[]{"bash", "-c", "pyenv local " + pyVersion + " && python -m venv " + venvPath};
        val proc = new ProcessBuilder(command).directory(workingDir.toFile()).start();

        // Read the output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[Python] {}", line.trim());
            }
        }

        // Read the errors
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("[Python] {}", errorLine);
            }
        }

        val exitCode = proc.waitFor();
        log.info("Finished (exit code {})", exitCode);

        return new Venv(venvPath, pyVersion);
    }

    public Venv installRequirements(final Path requirementsPath) throws IOException, InterruptedException {
        // bash -c python -m pip install -r requirements
        val command = new String[]{"bash", "-c", pathFor("bin/python") + " -m pip install -r " + requirementsPath};
        val proc = new ProcessBuilder(command).start();

        // Read the output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[Python] {}", line.trim());
            }
        }

        // Read the errors
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("[Python] {}", errorLine);
            }
        }

        val exitCode = proc.waitFor();
        log.info("Finished (exit code {})", exitCode);

        return this;
    }

    public Venv install(final List<String> requirements) throws IOException, InterruptedException {
        for (val requirement : requirements) {
            install(requirement);
        }

        return this;
    }

    public Venv install(final String requirement) throws IOException, InterruptedException {
        val command = new String[]{"bash", "-c", pathFor("bin/python") + " -m pip install " + requirement};
        val proc = new ProcessBuilder(command).start();

        // Read the output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("[Python] {}", line.trim());
            }
        }

        // Read the errors
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                log.error("[Python] {}", errorLine);
            }
        }

        val exitCode = proc.waitFor();
        log.info("Finished (exit code {})", exitCode);

        return this;
    }
}
