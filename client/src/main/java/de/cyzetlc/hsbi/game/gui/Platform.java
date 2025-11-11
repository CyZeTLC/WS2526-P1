package de.cyzetlc.hsbi.game.gui;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;

@Getter
public class Platform {
    private double x, y, width, height;
    private Pane pane;

    private static final String TILESET_PATH = "assets/tileset/dirt_tiles.png";
    private static final Image TILESET_IMAGE = new Image(TILESET_PATH);
    private static final int TILE_SIZE = 32;

    public Platform(double x, double y, double width, double height, Pane pane) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pane = pane;

        this.drawPlatform();
    }

    private void drawPlatform() {
        final int TILE_SIZE = 32;

        int fullTilesY = (int) (height / TILE_SIZE);

        int remainingHeight = (int) (height % TILE_SIZE);

        if (remainingHeight == 0) {
            remainingHeight = TILE_SIZE;
        } else {
            // Bei Rest unvollständige Kachel
            fullTilesY++;
        }

        int tilesX = (int) (width / TILE_SIZE);

        // Koordinaten der Kacheln im Tileset
        final int DIRT_TOP_TILE_Y = 32;   // TOP
        final int DIRT_BODY_TILE_Y = 0;   // DEFAULT

        // Die genaue vertikale Position der Kacheln im Spiel
        double currentY = this.y;

        for (int i = 0; i < tilesX; i++) {
            currentY = this.y; // Y-Koordinate für jede Spalte zurücksetzen

            for (int j = 0; j < fullTilesY; j++) {
                ImageView tileView = new ImageView(TILESET_IMAGE);

                // --- Logik zur Bestimmung des Kachelinhalts (Oberfläche vs. Untergrund) ---
                int tileSrcY;
                if (j == 0) {
                    tileSrcY = DIRT_TOP_TILE_Y; // Die oberste Kachel ist immer Gras
                } else {
                    tileSrcY = DIRT_BODY_TILE_Y; // Alles darunter ist reine Erde
                }

                // --- Logik für die unvollständige Kachel (letzte Kachel in der Spalte) ---
                double tileDrawHeight = TILE_SIZE;
                if (j == fullTilesY - 1 && height % TILE_SIZE != 0) {
                    // Wenn es die letzte Kachel ist UND wir einen Rest haben:
                    tileDrawHeight = remainingHeight;
                }

                // Viewport aus dem Tileset
                tileView.setViewport(new Rectangle2D(
                        0,            // x-Start im Tileset
                        tileSrcY,        // y-Start im Tileset
                        TILE_SIZE,       // Breite des Ausschnitts
                        tileDrawHeight   // Höhe des Ausschnitts
                ));

                // Setze die Position und Größe der Kachel
                tileView.setX(x + i * TILE_SIZE);
                tileView.setY(currentY);
                tileView.setFitWidth(TILE_SIZE);
                tileView.setFitHeight(tileDrawHeight);

                pane.getChildren().add(tileView);

                currentY += tileDrawHeight;
            }
        }
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(x, y, width, height);
    }
}

