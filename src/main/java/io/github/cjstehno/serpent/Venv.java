package io.github.cjstehno.serpent;

import io.github.cjstehno.serpent.CommandRunner.LoggingLineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.cjstehno.serpent.CommandRunner.forCommand;

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
        val version = new AtomicReference<String>();

        val cmd = forCommand(venvPath.resolve("bin/python") + " --version")
            .outputHandler(line -> version.set(line.split(" ")[1]))
            .errorHandler(new LoggingLineHandler(true));

        val exitCode = cmd.execute();
        log.info("Finished (exit code {})", exitCode);

        return new Venv(venvPath, version.get());
    }

    // FIXME: pyenv must be installed and accessible (with desired version installed)
    // NOTE: this operation takes some time
    public static Venv createVenv(final Path workingDir, final String pyVersion, final Path venvPath) throws IOException, InterruptedException {
        val cmd = forCommand("pyenv local " + pyVersion + " && python -m venv " + venvPath)
            .workingDir(workingDir)
            .outputHandler(new LoggingLineHandler(false))
            .errorHandler(new LoggingLineHandler(true));

        val exitCode = cmd.execute();
        log.info("Finished (exit code {})", exitCode);

        return new Venv(venvPath, pyVersion);
    }

    public Venv installRequirements(final Path requirementsPath) throws IOException, InterruptedException {
        val cmd = forCommand(pathFor("bin/python") + " -m pip install -r " + requirementsPath)
            .outputHandler(new LoggingLineHandler(false))
            .errorHandler(new LoggingLineHandler(true));

        val exitCode = cmd.execute();
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
        val cmd = forCommand(pathFor("bin/python") + " -m pip install " + requirement)
            .outputHandler(new LoggingLineHandler(false))
            .errorHandler(new LoggingLineHandler(true));

        val exitCode = cmd.execute();
        log.info("Finished (exit code {})", exitCode);

        return this;
    }
}
