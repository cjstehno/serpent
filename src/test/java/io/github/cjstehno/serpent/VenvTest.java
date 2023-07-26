package io.github.cjstehno.serpent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class VenvTest {

    private static final String PYTHON_VERSION = "3.11.4";

    @Test
    void creating(@TempDir final Path dir) throws Exception {
        // Create the venv directory
        val venvPath = dir.resolve(".venv");
        createDirectory(venvPath);

        // Create the requirements file
        val reqFile = createFile(venvPath.resolve("requirements.txt"));
        writeString(reqFile, "rich", CREATE);

        val venv = Venv.createVenv(dir, PYTHON_VERSION, venvPath);
        assertEquals(venv.path(), venvPath);
        assertEquals(venv.version(), PYTHON_VERSION);
        assertTrue(Files.exists(venv.pathFor("bin/python")));

        assertVenvPythonVersion(venvPath, PYTHON_VERSION);

        // install some requirements
        venv.installRequirements(reqFile);

        // FIXME: verify something
    }

    @Test void using(@TempDir final Path dir) throws Exception {
        // Create the venv directory
        val venvPath = dir.resolve(".venv");
        createDirectory(venvPath);

        // Create the requirements file
        val reqFile = createFile(venvPath.resolve("requirements.txt"));
        writeString(reqFile, "rich", CREATE);

        val venv = Venv.createVenv(dir, PYTHON_VERSION, venvPath);

        // now create a new instance using the created venv
        val otherVenv = Venv.useVenv(venv.path());
        assertVenvPythonVersion(otherVenv.path(), PYTHON_VERSION);

        assertTrue(Venv.venvExists(otherVenv.path()));
    }

    static void assertVenvPythonVersion(final Path venvPath, final String expectedVersion) throws Exception {
        val python = venvPath.resolve("bin/python");
        val proc = new ProcessBuilder(python.toString(), "--version").start();

        String versionLine = "";

        // Read the output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                versionLine = line.trim();
            }
        }

        assertTrue(proc.waitFor(1, MINUTES));
        assertEquals(0, proc.exitValue());
        assertTrue(versionLine.contains(expectedVersion));
    }
}

