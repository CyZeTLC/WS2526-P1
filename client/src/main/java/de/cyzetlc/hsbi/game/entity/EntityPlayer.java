package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Direction;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;

/**
 * Die {@code EntityPlayer}-Klasse repräsentiert die Hauptspieler-Charakter-Entität in der Spielwelt.
 * Sie erweitert die Basisklasse {@code Player} und fügt visuelle Elemente (Sprite, Namensschild)
 * sowie spezifische Logik für das Rendering und die Aktualisierung der Position relativ zur Kamera hinzu.
 *
 *
 * @see Player
 * @see GameScreen
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
@Getter
public class EntityPlayer extends Player {
    /**
     * Das JavaFX {@code ImageView}-Objekt, das zur Darstellung des visuellen Sprites des Spielers verwendet wird.
     */
    private ImageView sprite;
    /**
     * Der JavaFX {@code Text}-Knoten, der zur Anzeige des Namensschilds des Spielers über dem Sprite dient.
     */
    private Text nameTag;

    /**
     * Die aktuelle horizontale Bewegungsrichtung (z. B. WALK_LEFT oder WALK_RIGHT), die zur
     * Bestimmung der Spiegelung des Sprites verwendet wird.
     */
    @Setter
    private Direction direction;

    /**
     * Flag, das angibt, ob der Spieler derzeit in der Lage ist, Spieldateien oder Ziele einzusammeln.
     */
    @Setter
    private boolean canCollectFiles = false;

    /**
     * Konstruiert eine neue {@code EntityPlayer}-Instanz.
     */
    public EntityPlayer() {
        super();
    }

    /**
     * Aktualisiert die visuellen Komponenten des Spielers (Sprite und Namensschild) basierend auf der
     * Welt-Position des Spielers und dem aktuellen Kamera-Offset.
     * <p>
     * Diese Methode stellt sicher, dass die Bildschirmposition des Spielers für reibungsloses Scrollen korrekt ist,
     * und handhabt die Spiegelung des Sprites basierend auf dem {@code direction}-Attribut.
     */
    @Override
    public void update() {
        double camX = 0;
        double camY = 0;

        if (Game.getInstance() != null && Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen gameScreen) {
            camX = gameScreen.getCameraX();
            camY = gameScreen.getCameraY();
        }

        // Position vom Sprite & Nametag anpassen (mit Kameraversatz)
        double screenX = this.getLocation().getX() - camX;
        double screenY = this.getLocation().getY() - camY;

        this.sprite.setX(screenX);
        this.sprite.setY(screenY);

        double tagWidth = this.nameTag.prefWidth(-1);

        double centeredX = screenX + (this.getWidth() / 2) - (tagWidth / 2);
        double centeredY = screenY - 25;

        this.nameTag.setLayoutX(centeredX);
        this.nameTag.setLayoutY(centeredY);

        // Richtung wechseln
        if (direction != null) {
            switch (direction) {
                case WALK_LEFT -> sprite.setScaleX(-1);  // Spiegeln nach links
                case WALK_RIGHT -> sprite.setScaleX(1);   // Normal (nach rechts)
            }
        }
    }

    /**
     * Initialisiert und zeichnet den visuellen Sprite und das Namensschild des Spielers auf das Spiel-Pane.
     * <p>
     * Setzt die anfängliche Position, Größe und die Kollisions-Bounding-Box für die Entität.
     *
     * @param pane Das {@code Pane} (die Spielwurzel), zu dem die Elemente des Spielers hinzugefügt werden sollen.
     * @param x Die anfängliche Welt-X-Koordinate.
     * @param y Die anfängliche Welt-Y-Koordinate.
     * @return Die aktuelle {@code EntityPlayer}-Instanz zur Verkettung.
     */
    public EntityPlayer drawPlayer(Pane pane, double x, double y) {
        // Nametag zeichnen
        this.nameTag = UIUtils.drawText(pane, this.getDisplayName(), x, y - 25);
        this.nameTag.setLayoutX(x - this.nameTag.getLayoutBounds().getWidth() / 2);

        // Bild laden
        Image image = new Image(getClass().getResource("/assets/player.png").toExternalForm());
        this.sprite = new ImageView(image);

        // Größe anpassen
        this.sprite.setFitWidth(40);
        this.sprite.setFitHeight(60);

        // Startposition
        this.sprite.setX(x);
        this.sprite.setY(y);

        // Bounding Box (für Kollisionen)
        this.setWidth((float) this.sprite.getFitWidth());
        this.setHeight((float) this.sprite.getFitHeight());
        this.setLocation(new Location(x, y));

        pane.getChildren().add(this.sprite);
        return this;
    }
}