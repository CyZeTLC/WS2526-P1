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

@Getter
public class ScreenManager {
    private final Stage stage;
    private GuiScreen currentScreen;
    private long lastTime = 0;

    // FPS-Tracking
    private int frameCount = 0;
    private double timeSinceLastFps = 0;
    private double currentFps = 0;

    private boolean running = true;

    private InputManager inputManager;

    public ScreenManager(Stage stage) {
        this.stage = stage;
        this.stage.setScene(new Scene(new Pane(), 1200, 800));
        this.stage.setTitle("WS2526-P1");
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

    public void showScreen(GuiScreen screen) {
        this.currentScreen = screen;
        screen.initialize();
        Platform.runLater(() -> stage.getScene().setRoot(screen.getRoot()));
    }

    public void closeScreen() {
        Platform.runLater(stage::close);
    }
}
