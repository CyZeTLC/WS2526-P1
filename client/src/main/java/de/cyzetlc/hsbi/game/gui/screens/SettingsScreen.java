package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Music;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OperatingSystem;

import java.util.Random;

public class SettingsScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    private Text volumeLbl;
    private Button muteBtn;

    public SettingsScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @Override
    public void initialize() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");
        UIUtils.drawCenteredText(root, "Einstellungen", 0, 50, false).setId("menu-title");
        UIUtils.drawCenteredButton(root, "Zurück", 0, 360, false, "mainmenu-button", () -> {
            // wenn ingame zurück

            // sonst
            screenManager.showScreen(Game.getInstance().getMainMenuScreen());
        });
        UIUtils.drawText(root, "© Copyright CyZeTLC.DE & Phantomic", 10, height-20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width-210, height-20);

        this.setupSoundControls(width);
    }

    private void setupSoundControls(double width) {
        double panelWidth = 220;
        double panelHeight = 90;
        double x = width - panelWidth - 20;
        double y = 20;

        UIUtils.drawRect(root, x, y, panelWidth, panelHeight, Color.BLACK).setOpacity(0.55);
        Text title = UIUtils.drawText(root, "Sound", x + 10, y + 22);
        title.setFill(Color.WHITE);

        this.volumeLbl = UIUtils.drawText(root, "", x + 10, y + 45);
        this.volumeLbl.setFill(Color.WHITE);


        Slider volumeSlider = UIUtils.drawCenteredSlider(root, 0, 1,
                SoundManager.getVolume(),
                120,
                false
        );
        volumeSlider.getStyleClass().add("custom-slider");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            SoundManager.setVolume(vol);
            this.updateVolumeLabel();
        });

        this.muteBtn = UIUtils.drawCenteredButton(root, "", 0, 200, false, () -> {
            SoundManager.setMuted(!SoundManager.isMuted());
            if (SoundManager.isMuted()) {
                volumeSlider.setDisable(true);
            } else {
                volumeSlider.setDisable(false);
                volumeSlider.setValue(SoundManager.getVolume());
            }
            this.updateMuteButton();
        });

        this.updateVolumeLabel();
        this.updateMuteButton();
    }

    private void updateVolumeLabel() {
        int percent = (int) Math.round(SoundManager.getVolume() * 100);
        String muteSuffix = SoundManager.isMuted() ? " (stumm)" : "";
        this.volumeLbl.setText("Lautstaerke: " + percent + "%" + muteSuffix);
    }

    private void updateMuteButton() {
        this.muteBtn.setText(SoundManager.isMuted() ? "Sound AN" : "Mute");
    }

    @Override
    public void update(double delta) {

    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "Settings";
    }
}
