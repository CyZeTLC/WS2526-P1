package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
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

/**
 * The {@code SettingsScreen} provides an interface for the user to configure game options,
 * primarily focusing on audio controls like volume and muting.
 * <p>
 * This screen features an animated background and persistent navigation options
 * to return to the main game flow.
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see ScreenManager
 */
public class SettingsScreen implements GuiScreen {
    /**
     * The root container for all visual elements displayed on this screen.
     */
    private final Pane root = new Pane();

    /**
     * Reference to the ScreenManager, used for handling screen transitions.
     */
    private final ScreenManager screenManager;

    /**
     * Text label displaying the current volume level in percentage, including a mute suffix.
     */
    private Text volumeLbl;

    /**
     * Button used to toggle the sound system's mute state.
     */
    private Button muteBtn;

    /**
     * Slider control used to adjust the master volume level (0.0 to 1.0).
     */
    private Slider volumeSlider;

    /**
     * Constructs a new SettingsScreen.
     *
     * @param screenManager The screen manager instance responsible for handling screen transitions.
     */
    public SettingsScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initializes the Settings screen by drawing the background, title, navigation,
     * and setting up the sound control panel.
     */
    @Override
    public void initialize() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");
        UIUtils.drawCenteredText(root, "Einstellungen", 0, 50, false).setId("menu-title");
        UIUtils.drawCenteredButton(root, "Zurueck", 0, 360, false, "mainmenu-button", () -> {
            // wenn ingame zurueck

            // sonst
            screenManager.showScreen(Game.getInstance().getMainMenuScreen());
        });
        UIUtils.drawText(root, "(c) Copyright CyZeTLC.DE & Phantomic", 10, height - 20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width - 210, height - 20);

        this.setupSoundControls(width);
    }

    /**
     * Sets up and displays the sound control panel in the top-right corner of the screen.
     * <p>
     * This method draws the panel background, title, volume label, volume slider, and mute button.
     * It also attaches listeners to the slider and button to update the {@code SoundManager}
     * and the corresponding UI elements.
     *
     * @param width The current width of the screen, used for right-side alignment.
     */
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

        this.volumeSlider = UIUtils.drawCenteredSlider(root, 0, 1,
                SoundManager.getVolume(),
                120,
                false
        );
        this.volumeSlider.getStyleClass().add("custom-slider");
        this.volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            SoundManager.setVolume(vol);
            this.updateVolumeLabel();
        });

        this.muteBtn = UIUtils.drawCenteredButton(root, "", 0, 200, false, () -> {
            SoundManager.setMuted(!SoundManager.isMuted());
            this.volumeSlider.setDisable(SoundManager.isMuted());
            if (!SoundManager.isMuted()) {
                this.volumeSlider.setValue(SoundManager.getVolume());
            }
            this.updateMuteButton();
            this.updateVolumeLabel();
        });

        this.volumeSlider.setDisable(SoundManager.isMuted());
        this.updateVolumeLabel();
        this.updateMuteButton();
    }

    /**
     * Updates the text content of the volume label ({@code volumeLbl}) to reflect the
     * current sound volume percentage and the muted status.
     */
    private void updateVolumeLabel() {
        int percent = (int) Math.round(SoundManager.getVolume() * 100);
        String muteSuffix = SoundManager.isMuted() ? " (stumm)" : "";
        this.volumeLbl.setText("Lautstaerke: " + percent + "%" + muteSuffix);
    }

    /**
     * Updates the text displayed on the mute button ({@code muteBtn}) to reflect the
     * current sound state (e.g., "Sound an" if muted, "Stummschalten" if active).
     */
    private void updateMuteButton() {
        this.muteBtn.setText(SoundManager.isMuted() ? "Sound an" : "Stummschalten");
    }

    /**
     * Retrieves the root pane of the SettingsScreen.
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
     * @return The constant screen name "Settings".
     */
    @Override
    public String getName() {
        return "Settings";
    }
}
