package de.cyzetlc.hsbi.game.gui;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Platform {
    private double x, y, width, height;
    private Pane pane;

    private static final String[] FRAME_PATHS = {
            "/assets/movingplatform/zuschnitt1.png",
            "/assets/movingplatform/zuschnitt1_zustand1.png"
    };
    private static final double FRAME_DURATION_SECONDS = 0.6;
    private static final int TILE_SIZE = 128; // groesser, damit das Rack erkennbar bleibt

    private List<ImageView> tiles = new ArrayList<>();
    private final List<Image> frames = new ArrayList<>();
    private int currentFrame = 0;
    private double frameTimer = 0;
    private long lastFrameTimeNanos = 0L;

    private Location location;

    public Platform(double x, double y, double width, double height, Pane pane) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pane = pane;

        this.location = new Location(x, y);
    }

    public void drawPlatform() {
        this.loadFrames();

        // Breite aufteilen
        int fullTilesX = (int) (width / TILE_SIZE);
        int remainingWidth = (int) (width % TILE_SIZE);

        if (remainingWidth == 0) {
            remainingWidth = TILE_SIZE;
        } else {
            fullTilesX++; // rechts eine zusaetzliche Kachel
        }

        int fullTilesY = (int) (height / TILE_SIZE);
        int remainingHeight = (int) (height % TILE_SIZE);

        if (remainingHeight == 0) {
            remainingHeight = TILE_SIZE;
        } else {
            fullTilesY++;
        }

        for (int i = 0; i < fullTilesX; i++) {
            double currentY = this.y;

            // effektive Breite der Kachel (Standard oder Rest)
            double tileDrawWidth = TILE_SIZE;
            if (i == fullTilesX - 1 && width % TILE_SIZE != 0) {
                tileDrawWidth = remainingWidth;
            }

            for (int j = 0; j < fullTilesY; j++) {
                ImageView tileView = new ImageView(this.frames.get(0));

                double tileDrawHeight = TILE_SIZE;
                if (j == fullTilesY - 1 && height % TILE_SIZE != 0) {
                    tileDrawHeight = remainingHeight;
                }

                // Position setzen
                tileView.setX((x + i * TILE_SIZE) - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraX());
                tileView.setY(currentY - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraY());
                tileView.setFitWidth(tileDrawWidth);
                tileView.setFitHeight(tileDrawHeight);

                pane.getChildren().add(tileView);
                this.tiles.add(tileView);

                currentY += tileDrawHeight;
            }
        }
    }

    public void update(GameScreen gameScreen) {
        double camX = gameScreen.getCameraX();
        double camY = gameScreen.getCameraY();
        double screenWidth = gameScreen.getRoot().getWidth();
        double screenHeight = gameScreen.getRoot().getHeight();

        this.advanceAnimation();

        int fullTilesX = (int) (width / TILE_SIZE);
        int remainingWidth = (int) (width % TILE_SIZE);
        if (remainingWidth != 0) fullTilesX++;

        int fullTilesY = (int) (height / TILE_SIZE);
        int remainingHeight = (int) (height % TILE_SIZE);
        if (remainingHeight != 0) fullTilesY++;

        int index = 0;

        for (int i = 0; i < fullTilesX; i++) {
            double tileDrawWidth = (i == fullTilesX - 1 && width % TILE_SIZE != 0) ? remainingWidth : TILE_SIZE;
            double tileX = x + i * TILE_SIZE;
            // Pruefen, ob Tile horizontal sichtbar ist
            if (tileX + tileDrawWidth < camX || tileX > camX + screenWidth) {
                // Tile ist ausserhalb des Bildschirms, unsichtbar machen
                for (int j = 0; j < fullTilesY; j++) {
                    ImageView tileView = tiles.get(index++);
                    tileView.setVisible(false);
                }
                continue;
            }

            double currentY = y;
            for (int j = 0; j < fullTilesY; j++) {
                double tileDrawHeight = (j == fullTilesY - 1 && height % TILE_SIZE != 0) ? remainingHeight : TILE_SIZE;
                double tileY = currentY;

                ImageView tileView = tiles.get(index++);
                tileView.setVisible(true);
                tileView.setImage(this.frames.get(this.currentFrame));
                tileView.setX(tileX - camX);
                tileView.setY(tileY - camY);
                tileView.setFitWidth(tileDrawWidth);
                tileView.setFitHeight(tileDrawHeight);

                currentY += tileDrawHeight;
            }
        }
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(this.getLocation().getX(), this.getLocation().getY(), width, height);
    }

    private void loadFrames() {
        if (!this.frames.isEmpty()) {
            return;
        }
        for (String path : FRAME_PATHS) {
            try {
                Image image = new Image(getClass().getResource(path).toExternalForm());
                this.frames.add(image);
            } catch (Exception ignored) { }
        }
        if (this.frames.isEmpty()) {
            // Fallback: alter Tileset, falls Ressourcen fehlen
            Image fallback = new Image("assets/tileset/sandstone_tiles.png");
            this.frames.add(fallback);
        }
    }

    private void advanceAnimation() {
        if (this.frames.size() < 2) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastFrameTimeNanos == 0L) {
            this.lastFrameTimeNanos = now;
            return;
        }
        double deltaSeconds = (now - this.lastFrameTimeNanos) / 1_000_000_000.0;
        this.lastFrameTimeNanos = now;
        this.frameTimer += deltaSeconds;
        if (this.frameTimer >= FRAME_DURATION_SECONDS) {
            this.frameTimer -= FRAME_DURATION_SECONDS;
            this.currentFrame = (this.currentFrame + 1) % this.frames.size();
        }
    }
}

