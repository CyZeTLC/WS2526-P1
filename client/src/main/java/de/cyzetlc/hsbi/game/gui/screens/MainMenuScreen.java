package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
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

    //private final Text fpsLbl;

    public MainMenuScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawImage(root, "/assets/mainmenu.png", 0, 0, width, height);
        UIUtils.drawRect(root, 0, 0, width, height, Color.DARKGRAY).setOpacity(0.5);
        UIUtils.drawCenteredText(root, "STEAL THE FILES", 0, 50, false).setId("menu-title");
        UIUtils.drawCenteredButton(root, "Spiel starten", 0, 120, false, () -> screenManager.showScreen(new GameScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Mehrspieler", 0, 200, false, () -> screenManager.showScreen(new MultiplayerScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Beenden", 0, 280, false, screenManager::closeScreen);
        UIUtils.drawText(root, "© Copyright CyZeTLC.DE", 10, height-20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width-210, height-20);

        // Achievements
        UIUtils.drawRect(root, 60, 200, 400, height-400, Color.DARKGRAY).setOpacity(0.6);
        Text achievementsLbl = UIUtils.drawText(root, "Achievements", 200, 235, "achievements");
        achievementsLbl.setLayoutX((445-UIUtils.getTextWidth(achievementsLbl))/2);
        this.drawAchievementProgress();

        // Systeminfos
        /*UIUtils.drawRect(root, 60, 100, 400, height-200, Color.DARKGRAY).setOpacity(0.6);
        this.fpsLbl = UIUtils.drawText(root, "FPS: " + screenManager.getCurrentFps(), 75, 125);
        this.displaySystemInfo();*/
    }

    public void drawAchievementProgress() {
        for (int i = 0; i < 5; i++) {
            double progress = new Random().nextDouble(1);
            int fullWidth = 360;
            double greenWidth = 360 * progress;
            double blackWidth = fullWidth-greenWidth;
            UIUtils.drawText(root, "No. " + i + ": " + Math.round(progress*100) + "%", 80, 265 + i*60);
            UIUtils.drawRect(root, 80, 270 + i*60, greenWidth, 20, Color.GREEN);
            UIUtils.drawRect(root, 80+greenWidth, 270 + i*60, blackWidth, 20, Color.BLACK);
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
        UIUtils.drawText(root, "RAM (verfügbar): " + (memory.getAvailable() / (1024 * 1024 * 1024)) + "GB", 75, 245);
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
