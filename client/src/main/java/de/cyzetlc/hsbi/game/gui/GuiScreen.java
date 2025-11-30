package de.cyzetlc.hsbi.game.gui;

import javafx.scene.layout.Pane;

public interface GuiScreen {
    /**
     * Retrieves the root pane of the GameScreen, which contains all visual elements.
     *
     * @return The JavaFX {@code Pane} used as the root container.
     */
    Pane getRoot();

    /**
     * Returns the identifying name of this screen.
     *
     * @return The constant screen name "GameScreen".
     */
    String getName();

    void initialize();

    default void update(double delta) {}
}
