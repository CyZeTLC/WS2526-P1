package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import java.util.Random;

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

        UIUtils.drawAnimatedBackground(root, width, height,
                "/assets/hud/BackgroundMainZustand1.png",
                "/assets/hud/BackgroundMainZustand2.png",
                "/assets/hud/BackgroundMainZustand3.png");

        UIUtils.drawCenteredText(root, "STEAL THE FILES", 0, height / 2 - 230, false).setId("menu-title");
        UIUtils.drawCenteredButton(root, "Spiel starten", 0, height / 2 - 150, false, "mainmenu-button", () -> screenManager.showScreen(new GameScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Mehrspieler", 0, height / 2 - 70, false, "mainmenu-button", () -> screenManager.showScreen(new MultiplayerScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Einstellungen", 0, height / 2 + 10, false, "mainmenu-button", () -> screenManager.showScreen(new SettingsScreen(screenManager)));
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
