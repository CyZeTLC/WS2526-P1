package de.cyzetlc.hsbi.game.gui;

import javafx.scene.layout.Pane;

public interface GuiScreen {
    Pane getRoot();

    String getName();

    void initialize();

    default void update(double delta) {}
}
