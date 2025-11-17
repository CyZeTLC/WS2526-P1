package de.cyzetlc.hsbi.game.gui;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.block.Material;
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

    private static final String TILESET_PATH = Material.FLOOR.texturePath;
    private static final Image TILESET_IMAGE = new Image(TILESET_PATH);
    private static final int TILE_SIZE = 32;

    private List<ImageView> tiles = new ArrayList<>();

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
        final int TILE_SIZE = 32;

        // Breite aufteilen
        int fullTilesX = (int) (width / TILE_SIZE);
        int remainingWidth = (int) (width % TILE_SIZE);

        if (remainingWidth == 0) {
            remainingWidth = TILE_SIZE;
        } else {
            fullTilesX++; // rechts eine zusätzliche Kachel
        }

        int fullTilesY = (int) (height / TILE_SIZE);
        int remainingHeight = (int) (height % TILE_SIZE);

        if (remainingHeight == 0) {
            remainingHeight = TILE_SIZE;
        } else {
            fullTilesY++;
        }

        final int DIRT_TOP_TILE_Y = 0;
        final int DIRT_BODY_TILE_Y = 32;

        for (int i = 0; i < fullTilesX; i++) {
            double currentY = this.y;

            // Berechne die effektive Breite der Kachel (32 oder Rest)
            double tileDrawWidth = TILE_SIZE;
            if (i == fullTilesX - 1 && width % TILE_SIZE != 0) {
                tileDrawWidth = remainingWidth;
            }

            for (int j = 0; j < fullTilesY; j++) {
                ImageView tileView = new ImageView(TILESET_IMAGE);

                int tileSrcY = (j == 0) ? DIRT_TOP_TILE_Y : DIRT_BODY_TILE_Y;

                double tileDrawHeight = TILE_SIZE;
                if (j == fullTilesY - 1 && height % TILE_SIZE != 0) {
                    tileDrawHeight = remainingHeight;
                }

                // Viewport aus dem Tileset
                tileView.setViewport(new Rectangle2D(
                        0,            // x-Start im Tileset
                        tileSrcY,        // y-Start im Tileset
                        tileDrawWidth,   // Breite des Ausschnitts
                        tileDrawHeight   // Höhe des Ausschnitts
                ));

                // Setze die Position und Größe der Kachel
                //tileView.setX(x + i * TILE_SIZE);
                //tileView.setY(currentY);
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
            // Prüfen, ob Tile horizontal sichtbar ist
            if (tileX + tileDrawWidth < camX || tileX > camX + screenWidth) {
                // Tile ist außerhalb des Bildschirms, unsichtbar machen
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
}