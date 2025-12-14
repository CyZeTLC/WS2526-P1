package de.cyzetlc.hsbi.game.gui;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.input.InputManager;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Der {@code ScreenManager} ist die zentrale Komponente, die für die Steuerung der primären
 * JavaFX {@code Stage} (Fenster), die Verwaltung von Bildschirmübergängen ({@code GuiScreen}-Objekte)
 * und die Ausführung der zentralen Spiellogik-Schleife in einem separaten, dedizierten Thread verantwortlich ist.
 *
 * <p>
 * Er kümmert sich um die Fenstereinrichtung (z. B. Vollbild, Styling, Eingaberegistrierung) und stellt sicher,
 * dass Logik-Updates des Spiels (`update(delta)`) asynchron zum Rendering-Thread ausgeführt werden.
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 */
@Getter
public class ScreenManager {
    /**
     * Der konstante Titel, der für das Anwendungsfenster verwendet wird.
     */
    public static final String WINDOW_TITLE = "WS2526-P1-StealTheFiles";

    /**
     * Die primäre JavaFX Stage (Fenster)-Instanz, die von diesem Manager gesteuert wird.
     */
    private final Stage stage;

    /**
     * Der aktuell aktive Bildschirm, der angezeigt und aktualisiert wird.
     */
    private GuiScreen currentScreen;

    /**
     * Platzhalter für die Verfolgung der Frame-Zeit (die Hauptlogik wird innerhalb des dedizierten Game-Loop-Threads gehandhabt).
     */
    private long lastTime = 0;

    /**
     * Zähler für die Frames, die innerhalb des aktuellen Zeitintervalls gerendert wurden (wird für die FPS-Berechnung verwendet).
     */
    private int frameCount = 0;

    /**
     * Kumulierte Zeit seit der letzten FPS-Berechnung.
     */
    private double timeSinceLastFps = 0;

    /**
     * Die aktuell berechneten Frames pro Sekunde (FPS).
     */
    private double currentFps = 0;

    /**
     * Flag, das den Ausführungszustand des separaten Game-Loop-Threads steuert.
     */
    private boolean running = true;

    /**
     * Manager, der für die Registrierung und Verarbeitung aller Tastatur- und Mauseingaben verantwortlich ist.
     */
    private InputManager inputManager;

    /**
     * Ein Cache von Bildschirmen, die bereits initialisiert wurden, um redundante Einrichtung zu verhindern.
     */
    private List<GuiScreen> screenList = new ArrayList<>();

    /**
     * Konstruiert den ScreenManager, richtet die primäre JavaFX Stage ein und startet den Game-Loop-Thread.
     * <p>
     * Wichtige Einrichtungsschritte umfassen:
     * <ul>
     * <li>Konfiguration der Stage-Eigenschaften (Vollbild, Stil, Stylesheets, Icon).</li>
     * <li>Registrierung des globalen {@code InputManager}.</li>
     * <li>Starten eines Daemon-{@code gameLoop}-Threads zur Durchführung von Zeit- und Physikberechnungen
     * unabhängig vom JavaFX Rendering-Thread, wobei {@code Platform.runLater} verwendet wird, um
     * Bildschirm-Updates sicher aufzurufen.</li>
     * </ul>
     *
     * @param stage Die Haupt-Stage, die vom JavaFX-Anwendungseinstiegspunkt bereitgestellt wird.
     */
    public ScreenManager(Stage stage) {
        this.stage = stage;
        this.stage.setScene(new Scene(new Pane(), 1200, 800));
        this.stage.setTitle(WINDOW_TITLE);
        this.stage.getScene().getStylesheets().add(getClass().getResource("/assets/style.css").toExternalForm());
        this.stage.setResizable(false);
        this.stage.initStyle(StageStyle.UNDECORATED);
        this.stage.setFullScreen(true);
        this.stage.setFullScreenExitHint("");
        this.stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        this.stage.getIcons().add(new Image(getClass().getResource("/assets/icon.png").toExternalForm()));
        this.stage.show();

        this.inputManager = new InputManager();
        this.inputManager.register(this.stage.getScene());

        // Externer Thread für Game Loop (Logik unabhängig von JavaFX Render Thread)
        Thread gameLoop = new Thread(() -> {
            long lastTime = System.nanoTime();
            while (running) {
                long now = System.nanoTime();
                // Delta-Zeit in Sekunden
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // FPS berechnen
                frameCount++;
                timeSinceLastFps += delta;
                if (timeSinceLastFps >= 1.0) {
                    currentFps = frameCount / timeSinceLastFps;
                    frameCount = 0;
                    timeSinceLastFps = 0;
                }

                // Logik auf JavaFX Thread ausführen (sicherer Zugriff auf UI-Elemente)
                if (currentScreen != null) {
                    Platform.runLater(() -> currentScreen.update(delta));
                }

                try {
                    // Wartezeit, um eine hohe, aber nicht unbegrenzte Update-Rate zu erzielen
                    Thread.sleep(5); // ~200Hz
                } catch (InterruptedException ignored) {}
            }
        });

        gameLoop.setDaemon(true); // Thread beendet sich automatisch beim Schließen der Anwendung
        gameLoop.start();
    }

    /**
     * Wechselt die Ansicht der Anwendung zum angegebenen Bildschirm.
     * <p>
     * Wenn der Bildschirm noch nicht angezeigt wurde, wird seine {@code initialize()}-Methode aufgerufen.
     * Das Root-Pane des Bildschirms wird dann als Root der Scene der Haupt-Stage festgelegt.
     *
     * @param screen Das {@code GuiScreen}, das als Nächstes angezeigt werden soll.
     */
    public void showScreen(GuiScreen screen) {
        this.currentScreen = screen;
        // Initialisiere den Bildschirm nur, wenn er noch nicht im Cache ist
        if (!this.screenList.contains(screen)) {
            screen.initialize();
        }

        // Wenn es das Hauptmenü ist, wird der Zurück-Bildschirm gelöscht
        if (screen instanceof MainMenuScreen) {
            Game.getInstance().setBackScreen(null);
        }

        this.screenList.add(screen);
        // Setze das Root-Pane auf dem JavaFX Application Thread
        Platform.runLater(() -> stage.getScene().setRoot(screen.getRoot()));
    }

    /**
     * Schließt die JavaFX-Anwendungs-Stage sicher.
     * <p>
     * Dieser Vorgang wird auf dem JavaFX Application Thread eingeplant.
     */
    public void closeScreen() {
        Platform.runLater(stage::close);
    }
}