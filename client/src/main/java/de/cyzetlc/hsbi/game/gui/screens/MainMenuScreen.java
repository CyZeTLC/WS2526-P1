package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.message.MessageHandler;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;

/**
 * Der {@code MainMenuScreen} repräsentiert den primären Navigationsbildschirm für das Spiel.
 *
 * <p>
 * Er bietet Optionen zum Starten des Spiels, zum Zugriff auf den Mehrspieler-Modus, zum Konfigurieren der Einstellungen und zum Beenden.
 * Der Bildschirm verfügt über einen animierten, kontinuierlich scrollenden Hintergrund.
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see ScreenManager
 * @see Game
 */
public class MainMenuScreen implements GuiScreen {
    /**
     * Der Wurzel-Container für alle auf diesem Bildschirm angezeigten visuellen Elemente.
     */
    private final Pane root = new Pane();

    /**
     * Referenz auf den ScreenManager, der für die Behandlung von Bildschirmübergängen verwendet wird.
     */
    private final ScreenManager screenManager;

    /**
     * Konstruiert einen neuen MainMenuScreen.
     *
     * @param screenManager Die ScreenManager-Instanz, die für die Behandlung von Bildschirmübergängen verantwortlich ist.
     */
    public MainMenuScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initialisiert den Hauptmenü-Bildschirm, indem der animierte Hintergrund eingerichtet wird,
     * der Haupttitel gezeichnet wird, Navigationsschaltflächen erstellt werden und das Erfolgs-Panel initialisiert wird.
     * <p>
     * Die Methode erstellt eine {@code Timeline} für die Hintergrundanimation, die kontinuierlich
     * zwei Hintergrund-{@code ImageView}s horizontal verschiebt, um endloses Scrollen zu simulieren.
     */
    @Override
    public void initialize() {
        MessageHandler messageHandler = Game.getInstance().getMessageHandler();
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        /*
        Hintergrund mit Bewegung von links nach rechts (so ähnlich wie in Minecraft halt)
         */
        ImageView bg1 = UIUtils.drawImage(root, "/assets/hud/background.png", 0, 0, width, height);
        ImageView bg2 = UIUtils.drawImage(root, "/assets/hud/background.png", 0, 0, width, height);

        bg1.setFitWidth(width);
        bg1.setFitHeight(height);

        bg2.setFitWidth(width);
        bg2.setFitHeight(height);
        bg2.setTranslateX(width); // Zweites Bild direkt rechts neben dem ersten positionieren

        double speed = 1;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(16), e -> { // ca. 60 FPS

                    // beide verschieben sich nach rechts
                    bg1.setTranslateX(bg1.getTranslateX() + speed);
                    bg2.setTranslateX(bg2.getTranslateX() + speed);

                    // Wenn ein Bild rechts komplett raus ist -> an linke Position setzen
                    if (bg1.getTranslateX() >= width) {
                        bg1.setTranslateX(bg2.getTranslateX() - width);
                    }

                    if (bg2.getTranslateX() >= width) {
                        bg2.setTranslateX(bg1.getTranslateX() - width);
                    }
                })
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        UIUtils.drawCenteredText(root, messageHandler.getStaticMessage("gui.mainmenu.title"), 0, height / 2 - 230, false).setId("menu-title");

        // Schaltflächen
        UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.mainmenu.btn.start"), 0, height / 2 - 150, false, "mainmenu-button", () -> screenManager.showScreen(new GameScreen(screenManager)));
        UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.mainmenu.btn.multiplayer"), 0, height / 2 - 70, false, "mainmenu-button", () -> screenManager.showScreen(new CommunityScreen(screenManager))).setDisable(true);
        UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.mainmenu.btn.settings"), 0, height / 2 + 10, false, "mainmenu-button", () -> screenManager.showScreen(Game.getInstance().getSettingsScreen()));
        UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.mainmenu.btn.exit"), 0, height / 2 + 90, false, "mainmenu-button", screenManager::closeScreen);

        // Footer
        UIUtils.drawText(root, "(c) Copyright CyZeTLC.DE & Phantomic", 10, height - 20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width - 210, height - 20);

        // Achievements Panel
        UIUtils.drawRect(root, 60, height / 2 - 200, 400, 400, Color.valueOf("#222626")).setOpacity(0.6);
        Text achievementsLbl = UIUtils.drawText(root, "Achievements", 200, height / 2 - 165, "achievements");
        // Text zentrieren (Mitte des Panels ist bei 60 + 400/2 = 260. Abstand zum linken Rand ist (260 - Textbreite/2) - 60)
        achievementsLbl.setLayoutX((445 - UIUtils.getTextWidth(achievementsLbl)) / 2); // 445 ist die rechte Kante des Panels (60 + 400 - 15)
        this.drawAchievementProgress(height / 2 - 130);
    }

    /**
     * Zeichnet ein Platzhalter-Panel, das die Fortschrittsbalken für eine festgelegte Anzahl von Erfolgen (Achievements) anzeigt.
     * <p>
     * Diese Methode verwendet Zufallswerte, um den Fortschrittsprozentsatz für jeden Erfolg zu simulieren
     * und visualisiert ihn mithilfe von überlagerten grünen und schwarzen {@code Rectangle}s.
     *
     * @param y Die Start-Y-Koordinate für die erste Erfolgszeile.
     */
    public void drawAchievementProgress(double y) {
        for (int i = 0; i < 5; i++) {
            double progress = new Random().nextDouble(1); // Zufälliger Fortschritt (0.0 bis 1.0)
            int fullWidth = 360;
            double greenWidth = 360 * progress;
            double blackWidth = fullWidth - greenWidth;
            UIUtils.drawText(root, "No. " + i + ": " + Math.round(progress * 100) + "%", 80, y - 5 + i * 60);
            // Grüner Balken für den Fortschritt
            UIUtils.drawRect(root, 80, y + i * 60, greenWidth, 20, Color.GREEN);
            // Schwarzer Balken für den verbleibenden Teil
            UIUtils.drawRect(root, 80 + greenWidth, y + i * 60, blackWidth, 20, Color.BLACK);
        }
    }

    /**
     * Ruft das Wurzel-Pane des MainMenuScreens ab.
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
     * @return Der konstante Bildschirmname "MainMenu".
     */
    @Override
    public String getName() {
        return "MainMenu";
    }
}