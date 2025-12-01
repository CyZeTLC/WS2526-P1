package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code MainMenuScreen} represents the primary navigation screen for the game.
 * <p>
 * It provides options to start the game, access multiplayer, configure settings, and exit.
 * The screen features an animated, continuously scrolling background.
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
     * The root container for all visual elements displayed on this screen.
     */
    private final Pane root = new Pane();

    /**
     * Reference to the ScreenManager, used for handling screen transitions.
     */
    private final ScreenManager screenManager;

    /**
     * Constructs a new MainMenuScreen.
     *
     * @param screenManager The screen manager instance responsible for handling screen transitions.
     */
    public MainMenuScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initializes the Main Menu screen by setting up the animated background,
     * drawing the main title, creating navigation buttons, and initializing the achievement panel.
     * <p>
     * The method establishes a {@code Timeline} for the background animation, which continuously
     * shifts two background {@code ImageView}s horizontally to simulate endless scrolling.
     */
    @Override
    public void initialize() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        /*
        Background mit Bewegung von links nach rechts (so ähnlich wie in Minecraft halt)
         */
        ImageView bg1 = UIUtils.drawImage(root, "/assets/hud/background.png", 0, 0, width, height);
        ImageView bg2 = UIUtils.drawImage(root, "/assets/hud/background.png", 0, 0, width, height);

        bg1.setFitWidth(width);
        bg1.setFitHeight(height);

        bg2.setFitWidth(width);
        bg2.setFitHeight(height);
        bg2.setTranslateX(width);

        double speed = 1;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(16), e -> {

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

        UIUtils.drawCenteredText(root, "STEAL THE FILES", 0, height / 2 - 230, false).setId("menu-title");

        UIUtils.drawCenteredButton(root, "Spiel starten", 0, height / 2 - 150, false, "mainmenu-button", () -> screenManager.showScreen(new GameScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Mehrspieler", 0, height / 2 - 70, false, "mainmenu-button", () -> screenManager.showScreen(new MultiplayerScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Einstellungen", 0, height / 2 + 10, false, "mainmenu-button", () -> screenManager.showScreen(Game.getInstance().getSettingsScreen()));
        UIUtils.drawCenteredButton(root, "Beenden", 0, height / 2 + 90, false, "mainmenu-button", screenManager::closeScreen);
        UIUtils.drawText(root, "(c) Copyright CyZeTLC.DE & Phantomic", 10, height - 20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width - 210, height - 20);

        // Achievements
        UIUtils.drawRect(root, 60, height / 2 - 200, 400, 400, Color.valueOf("#222626")).setOpacity(0.6);
        Text achievementsLbl = UIUtils.drawText(root, "Achievements", 200, height / 2 - 165, "achievements");
        achievementsLbl.setLayoutX((445 - UIUtils.getTextWidth(achievementsLbl)) / 2);
        this.drawAchievementProgress(height / 2 - 130);
    }

    /**
     * Draws a placeholder panel showing the progress bars for a set number of achievements.
     * <p>
     * This method uses random values to simulate the progress percentage for each achievement
     * and visualizes it using overlaid green and black {@code Rectangle}s.
     *
     * @param y The starting Y-coordinate for the first achievement line.
     */
    public void drawAchievementProgress(double y) {
        for (int i = 0; i < 5; i++) {
            double progress = new Random().nextDouble(1);
            int fullWidth = 360;
            double greenWidth = 360 * progress;
            double blackWidth = fullWidth - greenWidth;
            UIUtils.drawText(root, "No. " + i + ": " + Math.round(progress * 100) + "%", 80, y - 5 + i * 60);
            UIUtils.drawRect(root, 80, y + i * 60, greenWidth, 20, Color.GREEN);
            UIUtils.drawRect(root, 80 + greenWidth, y + i * 60, blackWidth, 20, Color.BLACK);
        }
    }

    /**
     * Retrieves the root pane of the MainMenuScreen.
     *
     * @return The JavaFX {@code Pane} used as the root container.
     */
    @Override
    public Pane getRoot() {
        return root;
    }

    /**
     * Returns the identifying name of this screen.
     *
     * @return The constant screen name "MainMenu".
     */
    @Override
    public String getName() {
        return "MainMenu";
    }
}
