package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.message.MessageHandler;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Die Klasse {@code LoadingScreen} zeigt eine visuelle Ladefortschrittsanzeige an,
 * während essenzielle Spiel-Assets (Texturen, Materialien) im Hintergrund geladen werden.
 *
 * <p>
 * Dieser Bildschirm stellt sicher, dass alle notwendigen Ressourcen vorbereitet sind,
 * bevor der Benutzer zum Hauptmenü wechselt. Die Ladezeit wird primär mit einer
 * animierten Fortschrittsbalken-Timeline für ein reibungsloses Benutzererlebnis synchronisiert.
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see ScreenManager
 * @see Material
 */
public class LoadingScreen implements GuiScreen {
    /** Der Wurzel-Container für alle visuellen Elemente auf diesem Bildschirm. */
    private final Pane root = new Pane();
    /** Referenz auf den ScreenManager zur Handhabung von Bildschirmübergängen. */
    private final ScreenManager screenManager;

    /**
     * Konstruiert einen neuen LoadingScreen.
     *
     * @param screenManager Die ScreenManager-Instanz, die für die Behandlung von Bildschirmübergängen verantwortlich ist.
     */
    public LoadingScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initialisiert den LoadingScreen, richtet den Hintergrund, den Titel,
     * die Fortschrittsbalken-Animation ein und startet den asynchronen Asset-Lade-Thread.
     *
     * <p>
     * Die Methode führt die folgenden Hauptschritte durch:
     * <ul>
     * <li>Zeichnet den animierten Hintergrund und den zentrierten Text "Lade...".</li>
     * <li>Erstellt und visualisiert den statischen Hintergrund und den dynamischen Vordergrund des Fortschrittsbalkens.</li>
     * <li>Initialisiert eine {@code Timeline} für die Fortschrittsbalken-Animation (derzeit auf 3 Sekunden eingestellt).</li>
     * <li>Startet einen separaten Thread, um alle {@code Material}-Werte zu durchlaufen und deren Texturen zu laden/cachen.</li>
     * <li>Stellt den Abschluss-Handler der {@code Timeline} so ein, dass zum {@code MainMenuScreen} gewechselt wird.</li>
     * </ul>
     */
    @Override
    public void initialize() {
        MessageHandler messageHandler = Game.getInstance().getMessageHandler();
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");

        UIUtils.drawCenteredText(root, messageHandler.getMessageForLanguage("gui.loading.title"), 0, height/2 - 100, false).setId("menu-title");

        double barWidth = width/2;   // Endbreite des Balkens
        double barHeight = 60;

        // Hintergrund des Ladebalkens
        UIUtils.drawRect(root, width/2 - barWidth/2, height/2 - barHeight/2, barWidth, barHeight, Color.GRAY);

        // Dynamischer Fortschrittsbalken (startet bei 0 Breite)
        Rectangle progress = UIUtils.drawRect(root, width/2 - barWidth/2, height/2 - barHeight/2, 0, barHeight, Color.LIMEGREEN);

        Timeline timeline = new Timeline();

        // KeyValue: Animation der Breite von 0 bis barWidth
        KeyValue kvWidth = new KeyValue(progress.widthProperty(), barWidth);
        // KeyFrame: Die Animation dauert 3000 Millisekunden
        KeyFrame kf = new KeyFrame(Duration.millis(3000), kvWidth);

        timeline.getKeyFrames().add(kf);

        /*
         * Assets laden (asynchron)
         */
        new Thread() {
            @Override
            public void run() {
                // Alle Material-Texturen vorladen
                for (Material material : Material.values()) {
                    if (material.texturePath != null && !material.texturePath.isEmpty()) {
                        try {
                            var url = getClass().getResource(material.texturePath);
                            if (url != null) {
                                Image image = new Image(url.toExternalForm());
                                ImageAssets.cacheBlockImage(material, image);
                            }
                        } catch (Exception ignored) {
                            // Fehlendes Asset überspringen, um Absturz zu vermeiden
                        }
                    }
                }
                // Assets für den Spieler vorwärmen
                ImageAssets.warm();
                // Alle Sounds vorladen
                SoundManager.preloadAll();
            }
        }.start();

        // Nach Abschluss der Timeline zum Hauptmenü wechseln
        timeline.setOnFinished(e -> Game.getInstance().getScreenManager().showScreen(Game.getInstance().getMainMenuScreen()));

        timeline.play();

        // Footer-Informationen
        UIUtils.drawText(root, "© Copyright CyZeTLC.DE & Phantomic", 10, height-20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width-210, height-20);

    }

    /**
     * Ruft das Wurzel-Pane des LoadingScreens ab, das alle visuellen Elemente enthält.
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
     * @return Der konstante Bildschirmname "LoadingScreen".
     */
    @Override
    public String getName() {
        return "LoadingScreen";
    }
}