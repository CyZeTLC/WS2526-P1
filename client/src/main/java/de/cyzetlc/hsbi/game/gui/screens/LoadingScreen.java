package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.level.impl.SecondLevel;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class LoadingScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    public LoadingScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @Override
    public void initialize() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");

        UIUtils.drawCenteredText(root, "Lade..", 0, height/2 - 100, false).setId("menu-title");

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

    @Override
    public void update(double delta) {

    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "LoadingScreen";
    }
}
