package de.cyzetlc.hsbi.game.gui;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code Platform} class represents a solid, rectangular block of level geometry
 * that the player can interact with (stand on or collide with).
 * <p>
 * This class is responsible for visually rendering the platform by dividing its total
 * area into smaller, texture-mapped tiles based on a tileset. It dynamically updates
 * the position and visibility of these tiles based on the camera's location for optimized rendering.
 *
 * @author Tom Coombs
 * @author leonardo (aka. Phantomic)
 *
 * @see GameScreen
 * @see Material
 * @see Location
 */
@Getter
public class Platform {
    /**
     * The X-coordinate (world space) of the platform's top-left corner.
     * <p>
     * The Y-coordinate (world space) of the platform's top-left corner.
     * <p>
     * The total width of the platform.
     * <p>
     * The total height of the platform.
     */
    private double x, y, width, height;

    /**
     * The JavaFX {@code Pane} where the tiles are drawn as children.
     */
    private Pane pane;

    /**
     * The resource path for the tileset texture used to draw the platform.
     */
    private static final String TILESET_PATH = Material.FLOOR.texturePath;

    /**
     * The pre-loaded image containing all texture tiles.
     */
    private static final Image TILESET_IMAGE = ImageAssets.get(TILESET_PATH);

    /**
     * The standard width and height of a single tile in the tileset (in pixels).
     */
    private static final int TILE_SIZE = 32;

    /**
     * List of {@code ImageView} objects representing the individual tiles that make up the platform.
     */
    private List<ImageView> tiles = new ArrayList<>();

    /**
     * The world location of the platform's top-left corner, encapsulated as a {@code Location} object.
     */
    private Location location;

    /**
     * Constructs a new Platform instance.
     *
     * @param x The world X-coordinate of the platform.
     * @param y The world Y-coordinate of the platform.
     * @param width The total width of the platform.
     * @param height The total height of the platform.
     * @param pane The root pane where the platform tiles will be drawn.
     */
    public Platform(double x, double y, double width, double height, Pane pane) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.pane = pane;

        this.location = new Location(x, y);
    }

    /**
     * Draws the platform by calculating and creating individual tiles based on the platform's
     * dimensions, using the tileset texture.
     * <p>
     * This method handles the tiling logic, ensuring correct textures are used for the
     * top layer and body, and managing partial tiles if the dimensions are not multiples of {@code TILE_SIZE}.
     * It also performs an initial camera offset to position the tiles correctly.
     */
    public void drawPlatform() {
        if (pane != null && !tiles.isEmpty()) {
            pane.getChildren().removeAll(tiles);
            tiles.clear();
        }

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

    /**
     * Updates the position and visibility of the platform's tiles based on the current camera position.
     * <p>
     * This method iterates through all individual tiles and:
     * <ul>
     * <li>Checks if the tile is within the visible screen area (culling).</li>
     * <li>Sets the tile's visibility accordingly.</li>
     * <li>Adjusts the tile's screen coordinates by subtracting the camera's X and Y offset
     * (parallax effect/screen scrolling).</li>
     * </ul>
     * If the tile count is inconsistent (e.g., after loading), it triggers a rebuild.
     *
     * @param gameScreen The current active {@code GameScreen} instance, used to retrieve camera and screen dimensions.
     */
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

        int expectedTiles = fullTilesX * fullTilesY;
        if (tiles.size() != expectedTiles) {
            this.rebuildTiles();
            if (tiles.size() != expectedTiles) {
                return; // Avoid OOB if rebuild failed for some reason
            }
        }

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
            boolean failed = false;

            for (int j = 0; j < fullTilesY; j++) {
                double tileDrawHeight = (j == fullTilesY - 1 && height % TILE_SIZE != 0) ? remainingHeight : TILE_SIZE;
                double tileY = currentY;

                try {
                    ImageView tileView = tiles.get(index++);
                    tileView.setVisible(true);
                    tileView.setX(tileX - camX);
                    tileView.setY(tileY - camY);
                    tileView.setFitWidth(tileDrawWidth);
                    tileView.setFitHeight(tileDrawHeight);
                } catch (IndexOutOfBoundsException e) {
                    Game.getLogger().error(e.getMessage());
                    failed = true;
                    break;
                }

                currentY += tileDrawHeight;
            }

            if (failed) break;
        }
    }

    /**
     * Rebuilds the list of tiles and redraws the entire platform.
     * <p>
     * This is typically called when the current tile array size is inconsistent with the
     * expected size, often indicating a rendering glitch or a setup issue.
     */
    private void rebuildTiles() {
        if (pane != null && !tiles.isEmpty()) {
            pane.getChildren().removeAll(tiles);
        }
        tiles.clear();
        drawPlatform();
    }

    /**
     * Returns the rectangular bounds of the platform in world coordinates.
     *
     * @return A {@code Rectangle2D} object defining the collision area (X, Y, Width, Height).
     */
    public Rectangle2D getBounds() {
        return new Rectangle2D(this.getLocation().getX(), this.getLocation().getY(), width, height);
    }
}
