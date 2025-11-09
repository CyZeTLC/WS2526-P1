package de.cyzetlc.hsbi.game.listener;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.events.InputType;
import de.cyzetlc.hsbi.game.events.KeyInputEvent;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import javafx.scene.input.KeyCode;

public class KeyListener {
    @EventHandler
    public void handleKeyInput(KeyInputEvent e) {
        if (e.getInputType() == InputType.PRESSED) {
            if (e.getKeyCode() == KeyCode.ESCAPE) {
                if (Game.getInstance().getScreenManager().isRunning() &&
                        !Game.getInstance().getScreenManager().getCurrentScreen().getName().equals("MainMenu")) {
                    Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
                }
            }
        }
    }
}
