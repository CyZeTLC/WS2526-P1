package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.level.impl.SecondLevel;
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
 * The {@code LoadingScreen} class displays a visual loading progress bar
 * while essential game assets (textures, materials) are loaded in the background.
 * <p>
 * This screen ensures that all necessary resources are prepared before transitioning
 * the user to the main menu. The loading time is primarily synchronized with an
 * animated progress bar timeline for a smooth user experience.
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see ScreenManager
 * @see Material
 */
public class LoadingScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    /**
     * Constructs a new LoadingScreen.
     *
     * @param screenManager The screen manager instance responsible for handling screen transitions.
     */
    public LoadingScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initializes the LoadingScreen, sets up the background, the title,
     * the progress bar animation, and starts the asynchronous asset loading thread.
     * <p>
     * The method performs the following main steps:
     * <ul>
     * <li>Draws the animated background and centered "Lade..." text.</li>
     * <li>Creates and visualizes the static background and dynamic foreground of the progress bar.</li>
     * <li>Initializes a {@code Timeline} for the progress bar animation (currently set to 3 seconds).</li>
     * <li>Starts a separate thread to iterate through all {@code Material} values and load/cache their textures.</li>
     * <li>Sets the {@code Timeline}'s completion handler to switch to the {@code MainMenuScreen}.</li>
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

        UIUtils.drawRect(root, width/2 - barWidth/2, height/2 - barHeight/2, barWidth, barHeight, Color.GRAY);

        Rectangle progress = UIUtils.drawRect(root, width/2 - barWidth/2, height/2 - barHeight/2, 0, barHeight, Color.LIMEGREEN);

        Timeline timeline = new Timeline();

        KeyValue kvWidth = new KeyValue(progress.widthProperty(), barWidth);
        KeyFrame kf = new KeyFrame(Duration.millis(3000), kvWidth);

        timeline.getKeyFrames().add(kf);

        /*
         * Load assets
         */
        new Thread() {
            @Override
            public void run() {
                for (Material material : Material.values()) {
                    if (material.texturePath != null && !material.texturePath.isEmpty()) {
                        try {
                            var url = getClass().getResource(material.texturePath);
                            if (url != null) {
                                Image image = new Image(url.toExternalForm());
                                ImageAssets.cacheBlockImage(material, image);
                            }
                        } catch (Exception ignored) {
                            // skip missing asset to avoid crash
                        }
                    }
                    ImageAssets.warm();
                }
            }
        }.start();

        timeline.setOnFinished(e -> Game.getInstance().getScreenManager().showScreen(Game.getInstance().getMainMenuScreen()));

        timeline.play();

        UIUtils.drawText(root, "© Copyright CyZeTLC.DE & Phantomic", 10, height-20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width-210, height-20);

    }

    /**
     * Retrieves the root pane of the LoadingScreen, which contains all visual elements.
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
     * @return The constant screen name "LoadingScreen".
     */
    @Override
    public String getName() {
        return "LoadingScreen";
    }
}
