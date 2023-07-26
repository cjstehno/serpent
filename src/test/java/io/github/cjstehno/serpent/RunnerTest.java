package io.github.cjstehno.serpent;

import io.github.cjstehno.serpent.Script.FileScript;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.writeString;

class RunnerTest {

    private static final String PYTHON_VERSION = "3.11.4";

    @Test void running(@TempDir final Path dir) throws Exception {
        val venv = Venv.createVenv(dir, PYTHON_VERSION, dir.resolve(".venv"));

        val scriptPath = createFile(dir.resolve("hello.py"));
        writeString(
            scriptPath,
            """
                name = 'Java'
                print(f'Hello, {name}')            
                """
        );

        val runner = new Runner(venv, new FileScript(scriptPath));
        runner.run();

        // FIXME: capture output in useful manner?
        // FIXME: pass in args
    }

}