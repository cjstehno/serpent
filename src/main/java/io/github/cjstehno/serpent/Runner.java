package io.github.cjstehno.serpent;

import io.github.cjstehno.serpent.CommandRunner.LoggingLineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static io.github.cjstehno.serpent.CommandRunner.forCommand;

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
            val cmd = forCommand(venv.pathFor("bin/python") + " " + script.getPath())
                .outputHandler(new LoggingLineHandler(false))
                .errorHandler(new LoggingLineHandler(true));

            val code = cmd.execute();
            log.info("Finished with exit code: {}", code);

        } catch (Exception ex) {
            log.error("Bad things have happened: {}", ex.getMessage(), ex);
        }
    }
}
