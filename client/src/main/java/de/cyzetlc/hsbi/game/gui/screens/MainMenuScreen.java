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

public class MainMenuScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    private final Text fpsLbl;

    public MainMenuScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawCenteredText(root, "WS2526-P1 Menü", 0, 50, false).setId("menu-title");
        UIUtils.drawCenteredButton(root, "Spiel starten", 0, 120, false, () -> screenManager.showScreen(new GameScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Mehrspieler", 0, 200, false, () -> screenManager.showScreen(new MultiplayerScreen(screenManager)));
        UIUtils.drawCenteredButton(root, "Beenden", 0, 280, false, screenManager::closeScreen);
        UIUtils.drawRect(root, 60, 100, 400, height-200, Color.DARKGRAY);
        //UIUtils.drawImage(root, "/assets/pic.png", 60, 60, 380, 680);

        this.fpsLbl = UIUtils.drawText(root, "FPS: " + screenManager.getCurrentFps(), 75, 125);
        this.displaySystemInfo();
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
        this.fpsLbl.setText("FPS: " + (int) screenManager.getCurrentFps());
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
