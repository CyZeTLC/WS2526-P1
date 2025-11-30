package de.cyzetlc.hsbi.game.gui;

import javafx.scene.layout.Pane;

/**
 * The {@code GuiScreen} interface defines the contract for all screens within the game's graphical user interface (GUI).
 * <p>
 * Any class representing a state (e.g., Main Menu, Game Screen, Settings) must implement this interface,
 * providing a root pane for display and methods for initialization and state updates.
 *
 * @author Tom Coombs
 */
public interface GuiScreen {
    /**
     * Retrieves the root pane of the GameScreen, which contains all visual elements.
     * All components associated with this screen are children of this pane.
     *
     * @return The JavaFX {@code Pane} used as the root container.
     */
    Pane getRoot();

    /**
     * Returns the identifying name of this screen.
     *
     * @return The constant screen name (e.g., "MainMenuScreen", "GameScreen").
     */
    String getName();

    /**
     * Initializes the screen's state and visual components.
     * This method is called once when the screen is first loaded or activated,
     * typically used for loading resources, setting initial positions, and drawing static UI elements.
     */
    void initialize();

    /**
     * Updates the state of the screen. This method is part of the core game loop.
     * <p>
     * It is used to calculate physics, handle input, update animations, and check for state changes.
     * It is defined as a default method so that static screens (like simple menus) are not
     * forced to implement complex update logic.
     *
     * @param delta The time elapsed since the last frame, used for frame-rate independent calculations.
     */
    default void update(double delta) {}
}
