package de.cyzetlc.hsbi.game.listener;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.events.input.InputType;
import de.cyzetlc.hsbi.game.events.input.KeyInputEvent;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.gui.screens.SettingsScreen;
import javafx.scene.input.KeyCode;

public class KeyListener {
    @EventHandler
    public void handleKeyInput(KeyInputEvent e) {
        ScreenManager screenManager = Game.getInstance().getScreenManager();
        if (e.getInputType() == InputType.PRESSED) {
            if (e.getKeyCode() == KeyCode.ESCAPE) {
                if (screenManager.isRunning() &&
                        !screenManager.getCurrentScreen().getName().equals("Settings") &&
                        !screenManager.getCurrentScreen().getName().equals("LoadingScreen") &&
                        !screenManager.getCurrentScreen().getName().equals("MainMenu")) {
                    Game.getInstance().setBackScreen(Game.getInstance().getScreenManager().getCurrentScreen());
                    screenManager.showScreen(Game.getInstance().getSettingsScreen());
                } else if (screenManager.getCurrentScreen().getName().equals("Settings")) {
                    GuiScreen backScreen = Game.getInstance().getBackScreen();

                    if (backScreen != null) {
                        screenManager.showScreen(backScreen);
                    } else {
                        screenManager.showScreen(Game.getInstance().getMainMenuScreen());
                    }
                }
            } else if (e.getKeyCode() == KeyCode.F1) {
                if (screenManager.getCurrentScreen() instanceof GameScreen gameScreen) {
                    gameScreen.setShowTooltips(!gameScreen.isShowTooltips());
                }
            }
        }
    }
}
