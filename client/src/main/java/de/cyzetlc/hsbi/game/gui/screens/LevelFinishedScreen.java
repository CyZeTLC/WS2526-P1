package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.message.MessageHandler;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Der {@code LevelFinishedScreen} zeigt einen Übersichts-Bildschirm an, wenn der Spieler erfolgreich
 * ein Spiel-Level abgeschlossen hat.
 *
 * <p>
 * Dieser Bildschirm zeigt die Leistungsstatistiken des Spielers (benötigte Zeit, gesammelte Dateien,
 * verlorene Gesundheit) und bietet Optionen, um zum nächsten Level fortzufahren oder zum Hauptmenü zurückzukehren.
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see ScreenManager
 */
public class LevelFinishedScreen implements GuiScreen {
    /**
     * Der Wurzel-Container für alle auf diesem Bildschirm angezeigten visuellen Elemente.
     */
    private final Pane root = new Pane();

    /**
     * Referenz auf den ScreenManager, der für die Behandlung von Bildschirmübergängen verwendet wird.
     */
    private final ScreenManager screenManager;

    /**
     * Konstruiert einen neuen LevelFinishedScreen.
     *
     * @param screenManager Die ScreenManager-Instanz, die für die Behandlung von Bildschirmübergängen verantwortlich ist.
     */
    public LevelFinishedScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initialisiert den Level-Abgeschlossen-Bildschirm, richtet den Hintergrund, den Titel, die Statistiken
     * und die Navigationsschaltflächen ein.
     * <p>
     * Diese Methode berechnet die benötigte Zeit (Minuten und Sekunden) durch den Vergleich
     * der aktuellen Zeit mit der Startzeit des Levels und zeigt Statistiken
     * für gesammelte Dateien und verlorene Gesundheit an.
     */
    @Override
    public void initialize() {
        MessageHandler messageHandler = Game.getInstance().getMessageHandler();
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");

        // Zeichnet ein halbtransparentes schwarzes Rechteck als Hintergrund für die Statistiken
        UIUtils.drawRect(root, width/2 - 300, height/2 - 300, 600, 600, Color.BLACK).setOpacity(0.4);
        UIUtils.drawCenteredText(root, messageHandler.getMessageForLanguage("gui.finished.title"), 0, 50, false).setId("menu-title");

        // Schaltfläche "Nächstes Level" anzeigen, falls vorhanden
        if (Game.getInstance().getCurrentLevel().getNextLevel() != null) {
            UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.finished.btn.next"), 0, height / 2 + 150, false, "mainmenu-button", () -> {
                Game.getInstance().setCurrentLevel(Game.getInstance().getCurrentLevel().getNextLevel());
                Game.getInstance().getScreenManager().showScreen(new GameScreen(Game.getInstance().getScreenManager()));
                Game.getLogger().info(Game.getInstance().getCurrentLevel().getName() + " successfully loaded & saved!");
            });
        }
        // Schaltfläche "Zurück zum Menü"
        UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.finished.btn.mainmenu"), 0, height / 2 + 230, false, "mainmenu-button", () -> {
            Game.getInstance().getScreenManager().showScreen(Game.getInstance().getMainMenuScreen());
        });

        // Zeitberechnung
        long millis = System.currentTimeMillis() - Game.getInstance().getCurrentLevel().getLevelStarted();
        long secs = millis / 1000;
        long mins = secs / 60;
        long restsecs = secs % 60;

        // Gesammelte Dateien zählen
        int collected = Math.max(0, countFolderBlocks() - countActiveFolders());

        // Statistiken anzeigen
        UIUtils.drawCenteredText(root, messageHandler.getMessageForLanguage("gui.finished.level.title", Game.getInstance().getCurrentLevel().getName()), 0, 300, false, "stats-line-title");
        UIUtils.drawCenteredText(root, messageHandler.getMessageForLanguage("gui.finished.level.time", mins + ":" + restsecs), 0, 380, false, "stats-line");
        UIUtils.drawCenteredText(root, messageHandler.getMessageForLanguage("gui.finished.level.folder", String.valueOf(collected)), 0, 420, false, "stats-line");
        // Gesundheit verloren (Maximalgesundheit - aktuelle Gesundheit)
        UIUtils.drawCenteredText(root, messageHandler.getMessageForLanguage("gui.finished.level.health", String.valueOf((Game.thePlayer.getMaxHealth() - Game.thePlayer.getHealth()))), 0, 460, false, "stats-line");

        // Footer
        UIUtils.drawText(root, "© Copyright CyZeTLC.DE & Phantomic", 10, height-20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width-210, height-20);

    }

    /**
     * Zählt die Gesamtzahl der Ordnerblöcke (einsammelbare Dateien), die im aktuellen Level vorhanden sind.
     *
     * @return Die Gesamtzahl der {@code FolderBlock}-Instanzen in der Blockliste des Levels.
     */
    private int countFolderBlocks() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .count();
    }

    /**
     * Zählt die Anzahl der aktiven (nicht eingesammelten) Ordnerblöcke, die im aktuellen Level verbleiben.
     *
     * @return Die Anzahl der {@code FolderBlock}-Instanzen, die derzeit aktiv sind.
     */
    private int countActiveFolders() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .filter(de.cyzetlc.hsbi.game.gui.block.Block::isActive)
                .count();
    }

    /**
     * Ruft das Wurzel-Pane des LevelFinishedScreens ab.
     *
     * @return Das JavaFX {@code Pane}, das als Wurzel-Container verwendet wird.
     */
    @Override
    public Pane getRoot() {
        return root;
    }

    /**
     * Gibt den identifizierenden Namen dieses Bildschirms zurück.
     *
     * @return Der konstante Bildschirmname "LevelFinished".
     */
    @Override
    public String getName() {
        return "LevelFinished";
    }
}