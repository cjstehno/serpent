package io.github.cjstehno.serpent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Runner implements Runnable {

    /* FIxME:
        - specify or build venv
        - run with venv and args
     */

    private final Venv venv;
    private final String scriptPath;
    private final String[] arguments;

    public Runner(final Venv venv, final String scriptPath) {
        this(venv, scriptPath, new String[0]);
    }

    @Override public void run() {

    }
}
