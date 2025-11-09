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

public class MultiplayerScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    public MultiplayerScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        double height = screenManager.getStage().getScene().getHeight();
        double width = screenManager.getStage().getScene().getWidth();

        UIUtils.drawRect(root, 0, 0, width, 100, Color.DARKGRAY);
        UIUtils.drawRect(root, 0, height-100, width, 100, Color.DARKGRAY);
        UIUtils.drawCenteredText(root, "Mehrspieler", 0, 65, false).setId("menu-title");
        UIUtils.drawButton(root, "Zurück", 20, height-75, () -> screenManager.showScreen(new MainMenuScreen(screenManager)));

        this.drawServers();
    }

    @Override
    public void update(double delta) {

    }

    public void drawServers() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        for (int i = 0; i < 3; i++) {
            UIUtils.drawRect(root, width/2-400, 140 + i*115, 800, 100, Color.AZURE);
            UIUtils.drawText(root, "Server No. " + i, width/2-400 + 10, 140 + i*115 + 25, "server-label");
            UIUtils.drawText(root, "0/20", width/2+350, 140 + i*115 + 25, "server-label");
            UIUtils.drawCenteredText(root, "MOTD", 0, 140 + i*115 + 65, false, "server-label");
            //UIUtils.drawButton(root, "Beitreten",)
        }
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "Multiplayer";
    }
}
