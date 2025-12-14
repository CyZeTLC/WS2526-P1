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

/**
 * Der {@code KeyListener} ist ein zentraler Event-Handler, der auf Tastatureingaben reagiert.
 * <p>
 * Er implementiert systemweite Tastenbindungen, insbesondere für die {@code ESCAPE}-Taste,
 * um das Einstellungsmenü aufzurufen und zu navigieren, sowie für {@code F1} zur Steuerung
 * von Ingame-Elementen wie Tooltips.
 *
 *
 * @author Tom Coombs
 */
public class KeyListener {
    /**
     * Behandelt alle registrierten Tastatureingaben.
     * <p>
     * Die Logik konzentriert sich auf Aktionen, die beim *Drücken* einer Taste ausgeführt werden sollen.
     *
     * @param e Das ausgelöste {@code KeyInputEvent}.
     */
    @EventHandler
    public void handleKeyInput(KeyInputEvent e) {
        ScreenManager screenManager = Game.getInstance().getScreenManager();
        if (e.getInputType() == InputType.PRESSED) {
            if (e.getKeyCode() == KeyCode.ESCAPE) {
                // 1. Wenn ESC gedrückt wird und das Spiel läuft (nicht im Hauptmenü/Einstellungen):
                // -> Pausiere das Spiel und zeige die Einstellungen an.
                if (screenManager.isRunning() &&
                        !screenManager.getCurrentScreen().getName().equals("Settings") &&
                        !screenManager.getCurrentScreen().getName().equals("LoadingScreen") &&
                        !screenManager.getCurrentScreen().getName().equals("MainMenu")) {

                    // Speichere den aktuellen Bildschirm als 'BackScreen' (Rückkehrziel)
                    Game.getInstance().setBackScreen(Game.getInstance().getScreenManager().getCurrentScreen());
                    // Zeige den Einstellungsbildschirm
                    screenManager.showScreen(Game.getInstance().getSettingsScreen());

                    // 2. Wenn ESC gedrückt wird und wir uns bereits im Einstellungsbildschirm befinden:
                    // -> Gehe zurück zum letzten Bildschirm oder ins Hauptmenü.
                } else if (screenManager.getCurrentScreen().getName().equals("Settings")) {
                    GuiScreen backScreen = Game.getInstance().getBackScreen();

                    if (backScreen != null) {
                        // Gehe zurück zum gespeicherten Bildschirm (z.B. GameScreen)
                        screenManager.showScreen(backScreen);
                    } else {
                        // Fallback: Gehe zum Hauptmenü, falls kein BackScreen gesetzt ist
                        screenManager.showScreen(Game.getInstance().getMainMenuScreen());
                    }
                }
            } else if (e.getKeyCode() == KeyCode.F1) {
                // Umschalten der Tooltip-Sichtbarkeit im Spielbildschirm
                if (screenManager.getCurrentScreen() instanceof GameScreen gameScreen) {
                    gameScreen.setShowTooltips(!gameScreen.isShowTooltips());
                }
            }
        }
    }
}