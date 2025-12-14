package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

/**
 * Die abstrakte Klasse {@code Block} ist die Basisklasse für alle statischen und beweglichen Objekte
 * in der Spielwelt (z. B. Plattformen, Sammelobjekte, Feinde).
 * <p>
 * Sie definiert grundlegende Eigenschaften wie Material, Position, Größe, Kollidierbarkeit
 * und den visuellen Sprite.
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public abstract class Block {
    /**
     * Das {@code Material} des Blocks, das sein Aussehen und seine grundlegenden physikalischen Eigenschaften bestimmt.
     */
    @Getter @Setter
    private Material material;

    /**
     * Das JavaFX {@code Pane}, zu dem der Block hinzugefügt wurde.
     */
    @Getter
    private Pane pane;

    /**
     * Die JavaFX {@code ImageView}, die den visuellen Sprite des Blocks darstellt.
     */
    @Getter
    protected ImageView sprite;

    /**
     * Die Weltposition (obere linke Ecke) des Blocks.
     */
    @Getter @Setter
    private Location location;

    /**
     * Flag, das angibt, ob der Block Kollisionen mit dem Spieler verhindern kann (z. B. eine Wand).
     */
    @Getter @Setter
    private boolean collideAble;

    /**
     * Die Breite und Höhe der Kollisions-Bounding-Box in Welt-Koordinaten.
     */
    @Getter @Setter
    private double width, height;

    /**
     * Flag, das angibt, ob der Block aktiv ist (gezeichnet und aktualisiert wird).
     * Wenn {@code false}, sollte der Block aus dem Spiel entfernt werden.
     */
    @Getter
    private boolean active;

    /**
     * Die Verschiebung des Blocks in X- und Y-Richtung im letzten Update-Zyklus (wichtig für bewegliche Plattformen).
     */
    @Getter
    private double deltaX, deltaY;

    /**
     * Konstruiert einen neuen {@code Block} an der angegebenen Position.
     * <p>
     * Setzt Standardwerte für Größe (32x32) und Kollidierbarkeit (true).
     *
     * @param location Die anfängliche Weltposition des Blocks.
     */
    public Block(Location location) {
        this.location = location;
        this.width = 32;
        this.height = 32;
        this.collideAble = true;
        this.active = true;
        this.deltaX = 0;
        this.deltaY = 0;
    }

    /**
     * Abstrakte Methode, die die spezifische Logik ausführt, wenn ein Spieler mit diesem Block kollidiert.
     * <p>
     * Muss von Unterklassen implementiert werden, um Effekte wie Schaden, Sammeln oder Übergänge zu behandeln.
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    public abstract void onCollide(Player player);

    /**
     * Aktualisiert den Zustand des Blocks und seine Bildschirmposition relativ zum Kamera-Offset.
     */
    public void update() {
        if (Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen gameScreen) {
            // Bildschirmposition relativ zur Kamera setzen
            this.sprite.setX(this.getLocation().getX() - gameScreen.getCameraX());
            this.sprite.setY(this.getLocation().getY() - gameScreen.getCameraY());
        }
    }

    /**
     * Zeichnet den Block auf das Spiel-Pane.
     * <p>
     * Lädt den Sprite basierend auf dem Material, setzt dessen Größe und die anfängliche Position
     * (unter Berücksichtigung des Kamera-Offsets) und fügt ihn dem Pane hinzu.
     *
     * @param pane Das {@code Pane} (die Spielwurzel), zu dem der Block hinzugefügt werden soll.
     */
    public void draw(Pane pane) {
        this.sprite = ImageAssets.getBlockImage(this.getMaterial());
        this.pane = pane;

        double spriteWidth = this.width > 0 ? this.width : 32;
        double spriteHeight = this.height > 0 ? this.height : 32;

        // Spritegroesse anhand der Blockabmessungen setzen
        this.sprite.setFitWidth(spriteWidth);
        this.sprite.setFitHeight(spriteHeight);

        // Startposition
        if (Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen gameScreen) {
            this.sprite.setX(this.getLocation().getX() - gameScreen.getCameraX());
            this.sprite.setY(this.getLocation().getY() - gameScreen.getCameraY());
        }

        // Bounding Box (fuer Kollisionen)
        this.setWidth(this.sprite.getFitWidth());
        this.setHeight(this.sprite.getFitHeight());

        if (!pane.getChildren().contains(sprite)) {
            pane.getChildren().add(sprite);
        }
    }

    /**
     * Setzt den Aktivitätsstatus des Blocks.
     * <p>
     * Wenn {@code active} auf {@code false} gesetzt wird, entfernt diese Methode den Sprite vom Pane.
     *
     * @param active Der neue Aktivitätsstatus.
     */
    public void setActive(boolean active) {
        this.active = active;

        if (!active) {
            this.pane.getChildren().remove(this.sprite);
        }
    }

    /**
     * Gibt die Kollisions-Bounding-Box des Blocks in Welt-Koordinaten zurück.
     *
     * @return Eine {@code Rectangle2D}-Instanz, die die Grenzen des Blocks darstellt.
     */
    public Rectangle2D getBounds() {
        return new Rectangle2D(this.getLocation().getX(), this.getLocation().getY(), width, height);
    }

    /**
     * Setzt die Verschiebung (Delta) des Blocks im aktuellen Update-Zyklus.
     * Wird hauptsächlich von beweglichen Blöcken (z. B. {@code FloatingPlatformBlock}) verwendet.
     *
     * @param deltaX Die Verschiebung in X-Richtung.
     * @param deltaY Die Verschiebung in Y-Richtung.
     */
    protected void setDelta(double deltaX, double deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}