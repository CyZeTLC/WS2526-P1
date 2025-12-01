package de.cyzetlc.hsbi.game.gui;

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
 * The {@code ScreenManager} is the central component responsible for controlling the primary
 * JavaFX {@code Stage} (window), managing screen transitions ({@code GuiScreen} objects),
 * and running the core game logic loop on a separate, dedicated thread.
 * <p>
 * It handles window setup (e.g., fullscreen, styling, input registration) and ensures
 * that game logic updates (`update(delta)`) are executed asynchronously from the rendering thread.
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 */
@Getter
public class ScreenManager {
    /**
     * The constant title used for the application window.
     */
    public static final String WINDOW_TITLE = "WS2526-P1-StealTheFiles";

    /**
     * The primary JavaFX Stage (window) instance controlled by this manager.
     */
    private final Stage stage;

    /**
     * The currently active screen being displayed and updated.
     */
    private GuiScreen currentScreen;

    /**
     * Placeholder for frame time tracking (main logic is handled within the dedicated game loop thread).
     */
    private long lastTime = 0;

    /**
     * Counter for frames rendered within the current time interval (used for FPS calculation).
     */
    private int frameCount = 0;

    /**
     * Accumulated time since the last FPS calculation.
     */
    private double timeSinceLastFps = 0;

    /**
     * The currently calculated frames per second (FPS).
     */
    private double currentFps = 0;

    /**
     * Flag controlling the execution state of the separate game loop thread.
     */
    private boolean running = true;

    /**
     * Manager responsible for registering and processing all keyboard and mouse inputs.
     */
    private InputManager inputManager;

    /**
     * A cache of screens that have already been initialized, preventing redundant setup.
     */
    private List<GuiScreen> screenList = new ArrayList<>();

    /**
     * Constructs the ScreenManager, sets up the primary JavaFX Stage, and initiates the game loop thread.
     * <p>
     * Key setup steps include:
     * <ul>
     * <li>Configuring Stage properties (fullscreen, undecorated, stylesheets, icon).</li>
     * <li>Registering the global {@code InputManager}.</li>
     * <li>Starting a daemon {@code gameLoop} thread to perform time and physics calculations
     * independently of the JavaFX rendering thread, using {@code Platform.runLater} to
     * call screen updates safely.</li>
     * </ul>
     *
     * @param stage The main stage provided by the JavaFX application entry point.
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

                // Logik auf JavaFX Thread ausführen
                if (currentScreen != null) {
                    Platform.runLater(() -> currentScreen.update(delta));
                }

                try {
                    Thread.sleep(5); // ~200Hz
                } catch (InterruptedException ignored) {}
            }
        });

        gameLoop.setDaemon(true); // Thread beendet sich automatisch beim Schließen
        gameLoop.start();
    }

    /**
     * Switches the application's view to the specified screen.
     * <p>
     * If the screen has not been displayed before, its {@code initialize()} method is called.
     * The screen's root pane is then set as the root of the main stage's scene.
     *
     * @param screen The {@code GuiScreen} to display next.
     */
    public void showScreen(GuiScreen screen) {
        this.currentScreen = screen;
        if (!this.screenList.contains(screen)) {
            screen.initialize();
        }

        this.screenList.add(screen);
        Platform.runLater(() -> stage.getScene().setRoot(screen.getRoot()));
    }

    /**
     * Closes the JavaFX application stage safely.
     * <p>
     * This operation is scheduled on the JavaFX Application Thread.
     */
    public void closeScreen() {
        Platform.runLater(stage::close);
    }
}
