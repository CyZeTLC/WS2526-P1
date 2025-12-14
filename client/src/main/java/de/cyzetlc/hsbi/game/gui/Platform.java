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
 * Die Klasse {@code Platform} repräsentiert einen festen, rechteckigen Block der Level-Geometrie,
 * mit dem der Spieler interagieren kann (darauf stehen oder kollidieren).
 *
 * <p>
 * Diese Klasse ist dafür verantwortlich, die Plattform visuell darzustellen, indem ihre Gesamtfläche
 * in kleinere, texturierte Kacheln (Tiles) auf der Grundlage eines Kachel-Sets (Tileset) unterteilt wird.
 * Sie aktualisiert dynamisch die Position und Sichtbarkeit dieser Kacheln basierend auf dem Standort
 * der Kamera, um eine optimierte Darstellung zu gewährleisten.
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
     * Die X-Koordinate (Weltraum) der oberen linken Ecke der Plattform.
     * <p>
     * Die Y-Koordinate (Weltraum) der oberen linken Ecke der Plattform.
     * <p>
     * Die Gesamtbreite der Plattform.
     * <p>
     * Die Gesamthöhe der Plattform.
     */
    private double x, y, width, height;

    /**
     * Das JavaFX {@code Pane}, in das die Kacheln als Kindelemente gezeichnet werden.
     */
    private Pane pane;

    /**
     * Der Ressourcenpfad für die Tileset-Textur, die zum Zeichnen der Plattform verwendet wird.
     */
    private static final String TILESET_PATH = Material.FLOOR.texturePath;

    /**
     * Das vorgeladene Bild, das alle Textur-Kacheln enthält.
     */
    private static final Image TILESET_IMAGE = ImageAssets.get(TILESET_PATH);

    /**
     * Die Standardbreite und -höhe einer einzelnen Kachel im Tileset (in Pixeln).
     */
    private static final int TILE_SIZE = 32;

    /**
     * Liste von {@code ImageView}-Objekten, die die einzelnen Kacheln darstellen, aus denen die Plattform besteht.
     */
    private List<ImageView> tiles = new ArrayList<>();

    /**
     * Die Welt-Position der oberen linken Ecke der Plattform, gekapselt als {@code Location}-Objekt.
     */
    private Location location;

    /**
     * Konstruiert eine neue Platform-Instanz.
     *
     * @param x Die Welt-X-Koordinate der Plattform.
     * @param y Die Welt-Y-Koordinate der Plattform.
     * @param width Die Gesamtbreite der Plattform.
     * @param height Die Gesamthöhe der Plattform.
     * @param pane Das Root-Pane, in das die Plattform-Kacheln gezeichnet werden.
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
     * Zeichnet die Plattform, indem individuelle Kacheln basierend auf den Abmessungen der Plattform
     * unter Verwendung der Tileset-Textur berechnet und erstellt werden.
     * <p>
     * Diese Methode handhabt die Kachel-Logik, stellt sicher, dass die korrekten Texturen für
     * die oberste Schicht und den Körper verwendet werden, und verwaltet Teilkacheln, falls die Abmessungen
     * keine Vielfachen von {@code TILE_SIZE} sind. Sie führt auch einen anfänglichen Kamera-Offset
     * durch, um die Kacheln korrekt zu positionieren.
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

        final int DIRT_TOP_TILE_Y = 0; // Y-Koordinate für die Textur der obersten Schicht im Tileset
        final int DIRT_BODY_TILE_Y = 32; // Y-Koordinate für die Textur des Körpers im Tileset

        for (int i = 0; i < fullTilesX; i++) {
            double currentY = this.y;

            // Berechne die effektive Breite der Kachel (32 oder Rest)
            double tileDrawWidth = TILE_SIZE;
            if (i == fullTilesX - 1 && width % TILE_SIZE != 0) {
                tileDrawWidth = remainingWidth;
            }

            for (int j = 0; j < fullTilesY; j++) {
                ImageView tileView = new ImageView(TILESET_IMAGE);

                // Wähle die Textur: oberste Reihe oder Körper
                int tileSrcY = (j == 0) ? DIRT_TOP_TILE_Y : DIRT_BODY_TILE_Y;

                // Berechne die effektive Höhe der Kachel (32 oder Rest)
                double tileDrawHeight = TILE_SIZE;
                if (j == fullTilesY - 1 && height % TILE_SIZE != 0) {
                    tileDrawHeight = remainingHeight;
                }

                // Viewport aus dem Tileset festlegen
                tileView.setViewport(new Rectangle2D(
                        0,            // x-Start im Tileset (immer 0, da nur eine Spalte)
                        tileSrcY,        // y-Start im Tileset
                        tileDrawWidth,   // Breite des Ausschnitts
                        tileDrawHeight   // Höhe des Ausschnitts
                ));

                // Setze die Position und Größe der Kachel (Kamera-Offset anwenden)
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
     * Aktualisiert die Position und Sichtbarkeit der Kacheln der Plattform basierend auf der aktuellen Kameraposition.
     * <p>
     * Diese Methode iteriert durch alle einzelnen Kacheln und:
     * <ul>
     * <li>Prüft, ob die Kachel innerhalb des sichtbaren Bildschirmbereichs liegt (Culling).</li>
     * <li>Setzt die Sichtbarkeit der Kachel entsprechend.</li>
     * <li>Passt die Bildschirmkoordinaten der Kachel an, indem der X- und Y-Offset der Kamera subtrahiert wird
     * (Parallaxeneffekt/Bildschirm-Scrolling).</li>
     * </ul>
     * Wenn die Kachelanzahl inkonsistent ist (z. B. nach dem Laden), wird ein Neuaufbau ausgelöst.
     *
     * @param gameScreen Die aktuell aktive {@code GameScreen}-Instanz, wird verwendet, um Kamera- und Bildschirmabmessungen abzurufen.
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
                return; // Vermeide OOB (Index außerhalb der Grenzen), falls Neuaufbau fehlschlägt
            }
        }

        int index = 0;

        for (int i = 0; i < fullTilesX; i++) {
            double tileDrawWidth = (i == fullTilesX - 1 && width % TILE_SIZE != 0) ? remainingWidth : TILE_SIZE;
            double tileX = x + i * TILE_SIZE;
            // Prüfen, ob Tile horizontal sichtbar ist (Culling)
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
                    // Position basierend auf Kamera-Offset aktualisieren
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
     * Baut die Liste der Kacheln neu auf und zeichnet die gesamte Plattform neu.
     * <p>
     * Dies wird typischerweise aufgerufen, wenn die Größe des aktuellen Kachel-Arrays nicht mit der
     * erwarteten Größe übereinstimmt, was oft auf einen Rendering-Fehler oder ein Einrichtungsproblem hindeutet.
     */
    private void rebuildTiles() {
        if (pane != null && !tiles.isEmpty()) {
            pane.getChildren().removeAll(tiles);
        }
        tiles.clear();
        drawPlatform();
    }

    /**
     * Gibt die rechteckigen Begrenzungen der Plattform in Weltkoordinaten zurück.
     *
     * @return Ein {@code Rectangle2D}-Objekt, das den Kollisionsbereich (X, Y, Breite, Höhe) definiert.
     */
    public Rectangle2D getBounds() {
        return new Rectangle2D(this.getLocation().getX(), this.getLocation().getY(), width, height);
    }
}