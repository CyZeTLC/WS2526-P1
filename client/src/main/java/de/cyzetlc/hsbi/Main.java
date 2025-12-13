package de.cyzetlc.hsbi;

import de.cyzetlc.hsbi.game.Game;

/**
 * The {@code Main} class serves as the standard entry point for the entire application.
 * <p>
 * Its sole responsibility is to delegate the execution to the core {@code Game} class,
 * which manages the JavaFX application lifecycle and initialization.
 *
 * @see Game
 * @author Tom Coombs
 */
public class Main {
    /**
     * The application's main entry method, which is executed upon starting the Java program.
     * <p>
     * This method directly calls the static main method of the {@code Game} class,
     * effectively bootstrapping the JavaFX application.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        Game.main(args);
    }
}