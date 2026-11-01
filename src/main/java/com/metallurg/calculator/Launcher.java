package com.metallurg.calculator;

/**
 * Точка входа для fat-jar: JavaFX требует отдельный launcher вне Application.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
