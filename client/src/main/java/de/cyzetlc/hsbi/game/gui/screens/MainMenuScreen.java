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

public class MainMenuScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    public MainMenuScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

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

    public void displaySystemInfo() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        CentralProcessor cpu = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();

        UIUtils.drawText(root, String.valueOf(os), 75, 145);
        UIUtils.drawText(root, "CPU: " + cpu.getProcessorIdentifier().getName(), 75, 165);
        UIUtils.drawText(root, "Kerne (physisch): " + cpu.getPhysicalProcessorCount(), 75, 185);
        UIUtils.drawText(root, "Kerne (logisch): " + cpu.getLogicalProcessorCount(), 75, 205);
        UIUtils.drawText(root, "RAM (gesamt): " + (memory.getTotal() / (1024 * 1024 * 1024)) + "GB", 75, 225);
        UIUtils.drawText(root, "RAM (verfuegbar): " + (memory.getAvailable() / (1024 * 1024 * 1024)) + "GB", 75, 245);
    }

    @Override
    public void update(double delta) {
        //this.fpsLbl.setText("FPS: " + (int) screenManager.getCurrentFps());
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "MainMenu";
    }
}
