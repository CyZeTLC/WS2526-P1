package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.message.MessageHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Der {@code SettingsScreen} bietet eine Schnittstelle, über die der Benutzer Spieloptionen konfigurieren kann,
 * wobei der Schwerpunkt primär auf Audio-Steuerelementen wie Lautstärke und Stummschaltung liegt.
 *
 * <p>
 * Dieser Bildschirm verfügt über einen animierten Hintergrund und dauerhafte Navigationsoptionen,
 * um zum Haupt-Spielfluss zurückzukehren.
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see ScreenManager
 */
public class SettingsScreen implements GuiScreen {
    /**
     * Der Wurzel-Container für alle auf diesem Bildschirm angezeigten visuellen Elemente.
     */
    private final Pane root = new Pane();

    /**
     * Referenz auf den ScreenManager, der für die Behandlung von Bildschirmübergängen verwendet wird.
     */
    private final ScreenManager screenManager;

    /**
     * Text-Label, das den aktuellen Lautstärkepegel in Prozent anzeigt, einschließlich eines Stumm-Suffixes.
     */
    private Text volumeLbl;

    /**
     * Schaltfläche zum Umschalten des Stumm-Zustands des Soundsystems.
     */
    private Button muteBtn;

    /**
     * Schieberegler (Slider) zur Anpassung des Master-Lautstärkepegels (0.0 bis 1.0).
     */
    private Slider volumeSlider;

    /**
     * Konstruiert einen neuen SettingsScreen.
     *
     * @param screenManager Die ScreenManager-Instanz, die für die Behandlung von Bildschirmübergängen verantwortlich ist.
     */
    public SettingsScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initialisiert den Einstellungsbildschirm, indem der Hintergrund, der Titel, die Navigation
     * gezeichnet und das Sound-Bedienfeld eingerichtet werden.
     */
    @Override
    public void initialize() {
        MessageHandler messageHandler = Game.getInstance().getMessageHandler();
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");
        UIUtils.drawCenteredText(root, messageHandler.getMessageForLanguage("gui.settings.title"), 0, 50, false).setId("menu-title");


        // Schaltfläche DE (Deutsch)
        UIUtils.drawCenteredButton(root, "Deutsch", width / 2 + 200, 260, false,"mainmenu-button", () -> {
            messageHandler.applyLanguage("de");
            screenManager.showScreen(Game.getInstance().getSettingsScreen());
        });

        // Schaltfläche EN (Englisch)
        UIUtils.drawCenteredButton(root, "English", width / 2 + 200, 260+80,false,"mainmenu-button", () -> {
            messageHandler.applyLanguage("en");
            screenManager.showScreen(Game.getInstance().getSettingsScreen());
        });

        // Schaltfläche RU (Russisch)
        UIUtils.drawCenteredButton(root, "Русский", width / 2 + 200, 260+160, false,"mainmenu-button", () -> {
            messageHandler.applyLanguage("ru");
            screenManager.showScreen(Game.getInstance().getSettingsScreen());
        });

        // Schaltfläche Hauptmenü
        UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.settings.btn.mainmenu"), width / 2 - 600, 360+240, false,"mainmenu-button", () -> {
            Game.getInstance().setBackScreen(null);
            screenManager.showScreen(Game.getInstance().getMainMenuScreen());
        });
        // Schaltfläche Zurück
        UIUtils.drawCenteredButton(root, messageHandler.getMessageForLanguage("gui.settings.btn.back"), width / 2 - 600, 360+320, false, "mainmenu-button", () -> {
            GuiScreen backScreen = Game.getInstance().getBackScreen();

            if (backScreen != null) {
                screenManager.showScreen(backScreen);
            } else {
                screenManager.showScreen(Game.getInstance().getMainMenuScreen());
            }
        });
        // Footer-Informationen
        UIUtils.drawText(root, "(c) Copyright CyZeTLC.DE & Phantomic", 10, height - 20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width - 210, height - 20);

        this.setupSoundControls(width);
    }

    /**
     * Richtet das Sound-Bedienfeld in der oberen rechten Ecke des Bildschirms ein und zeigt es an.
     *
     * <p>
     * Diese Methode zeichnet den Panel-Hintergrund, den Titel, das Lautstärke-Label, den Lautstärke-Schieberegler und die Stumm-Taste.
     * Sie fügt außerdem Listener an den Schieberegler und die Schaltfläche an, um den {@code SoundManager}
     * und die entsprechenden UI-Elemente zu aktualisieren.
     *
     * @param width Die aktuelle Breite des Bildschirms, die für die Ausrichtung auf der rechten Seite verwendet wird.
     */
    private void setupSoundControls(double width) {
        double panelWidth = 220;
        double panelHeight = 90;
        double x = width - panelWidth - 20;
        double y = 20;

        // Panel-Hintergrund
        UIUtils.drawRect(root, x, y, panelWidth, panelHeight, Color.BLACK).setOpacity(0.55);
        Text title = UIUtils.drawText(root, "Sound", x + 10, y + 22);
        title.setFill(Color.WHITE);

        // Lautstärke-Label
        this.volumeLbl = UIUtils.drawText(root, "", x + 10, y + 45);
        this.volumeLbl.setFill(Color.WHITE);

        // Lautstärke-Schieberegler
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

        // Stumm-Taste
        this.muteBtn = UIUtils.drawCenteredButton(root, "", 0, 200, false, () -> {
            SoundManager.setMuted(!SoundManager.isMuted());
            this.volumeSlider.setDisable(SoundManager.isMuted());
            if (!SoundManager.isMuted()) {
                // Bei "Sound an" den vorherigen Wert wiederherstellen
                this.volumeSlider.setValue(SoundManager.getVolume());
            }
            this.updateMuteButton();
            this.updateVolumeLabel();
        });

        // Positionierung des Sliders und der Mute-Taste
        // (Die ursprünglichen Methoden zur Positionierung wurden entfernt, da sie den Codefluss unterbrochen haben. Die Logik muss hier ergänzt werden, falls nötig.)

        // Temporäre manuelle Positionierung (angenommene Positionen, da die originalen Methoden aus UIUtils nicht verfügbar sind)
        this.volumeSlider.setLayoutX(x + 10);
        this.volumeSlider.setLayoutY(y + 60);
        this.muteBtn.setLayoutX(x + 10);
        this.muteBtn.setLayoutY(y + 110);


        this.volumeSlider.setDisable(SoundManager.isMuted());
        this.updateVolumeLabel();
        this.updateMuteButton();
    }

    /**
     * Aktualisiert den Textinhalt des Lautstärke-Labels ({@code volumeLbl}), um den
     * aktuellen Lautstärke-Prozentsatz und den Stumm-Status widerzuspiegeln.
     */
    private void updateVolumeLabel() {
        int percent = (int) Math.round(SoundManager.getVolume() * 100);
        String muteSuffix = SoundManager.isMuted() ? " (stumm)" : "";
        this.volumeLbl.setText("Lautstaerke: " + percent + "%" + muteSuffix);
    }

    /**
     * Aktualisiert den Text, der auf der Stumm-Taste ({@code muteBtn}) angezeigt wird, um den
     * aktuellen Sound-Zustand widerzuspiegeln (z. B. "Sound an", wenn stummgeschaltet, "Stummschalten", wenn aktiv).
     */
    private void updateMuteButton() {
        MessageHandler messageHandler = Game.getInstance().getMessageHandler();
        this.muteBtn.setText(SoundManager.isMuted() ? messageHandler.getMessageForLanguage("gui.settings.btn.sound_on") : messageHandler.getMessageForLanguage("gui.settings.btn.sound_mute"));
    }

    /**
     * Ruft das Wurzel-Pane des SettingsScreens ab.
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
     * @return Der konstante Bildschirmname "Settings".
     */
    @Override
    public String getName() {
        return "Settings";
    }
}