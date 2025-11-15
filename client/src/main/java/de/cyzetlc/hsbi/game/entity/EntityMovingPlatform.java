package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;

/**
 * Repräsentiert eine bewegliche Plattform, die der Spieler als Untergrund nutzen kann.
 *
 * <p>Die Plattform orientiert sich stilistisch an {@link EntityPlayer} und stellt die gleichen
 * Lifecycle-Methoden zur Verfügung: Über {@link #drawPlatform(Pane, double, double, double, double)}
 * wird die Plattform erzeugt und auf dem Bildschirm platziert, {@link #update()} kümmert sich um
 * die kontinuierliche Bewegung, und Hilfsmethoden wie {@link #supportsPlayer(EntityPlayer)} helfen
 * bei der Kollisionserkennung.</p>
 */
@Getter
public class EntityMovingPlatform extends Entity {
    /** Grafische Repräsentation der Plattform auf der JavaFX-Bühne. */
    private ImageView sprite;

    /** Linke Grenze des Bewegungsbereichs auf der X-Achse. */
    private double minX;

    /** Rechte Grenze des Bewegungsbereichs auf der X-Achse. */
    private double maxX;

    /** Anzahl der Pixel, um die sich die Plattform pro Update verschiebt. */
    private double speed;

    /** Legt fest, ob sich die Plattform aktuell nach rechts bewegt. */
    private boolean movingRight = true;

    /** Ausgangs-Y-Position, von der aus der Schwebe-Effekt berechnet wird. */
    private double baseY;

    /** Maximale Amplitude der vertikalen Schwebebewegung. */
    private double hoverAmplitude = 6;

    /** Schrittweite für den Sinus-Fortschritt pro Update (je kleiner, desto langsamer). */
    private double hoverStep = 0.04;

    /** Aktuelle Zeitbasis für den Sinus (wird pro Update hochgezählt). */
    private double hoverTick = 0;

    /**
     * Aktualisiert Position, Hover-Animation und Bounding-Box der Plattform.
     *
     * <p>Die Methode wird typischerweise pro Frame vom Spiel-Loop aufgerufen. Sie sorgt dafür,
     * dass sich die Plattform zwischen den Grenzen {@link #minX} und {@link #maxX} hin- und
     * herbewegt und gleichzeitig sanft schwebt.</p>
     */
    @Override
    public void update() {
        // Ohne Sprite und gesetzte Startposition kann keine Bewegung erfolgen.
        if (this.sprite == null || this.getLocation() == null) {
            return;
        }

        // Ausgangspunkt: aktuelle Position aus dem Entity-State lesen.
        double currentX = this.getLocation().getX();

        // Je nach Bewegungsrichtung wird die X-Position inkrementiert bzw. dekrementiert.
        double nextX = currentX + (this.movingRight ? this.speed : -this.speed);

        // Wird die linke Grenze unterschritten, drehen wir die Richtung um und klemmen auf minX.
        if (nextX <= this.minX) {
            nextX = this.minX;
            this.movingRight = true;
        }

        // Wird die rechte Grenze überschritten, ebenfalls Richtung umkehren und einklemmen.
        double maxAllowedX = this.maxX - this.getWidth();
        if (nextX >= maxAllowedX) {
            nextX = maxAllowedX;
            this.movingRight = false;
        }

        // Für den Schwebe-Effekt erhöhen wir den Sinus-Tick und berechnen einen Offset.
        this.hoverTick += this.hoverStep;
        double hoverOffset = Math.sin(this.hoverTick) * this.hoverAmplitude;

        // Die endgültige Y-Position basiert auf der Basislinie plus Hover-Offset.
        double nextY = this.baseY + hoverOffset;

        // Sprite im Pane verschieben, damit die Animation sichtbar wird.
        this.sprite.setX(nextX);
        this.sprite.setY(nextY);

        // Die Bounding-Box des Entities (Location + Größe) mit der neuen Position synchronisieren.
        this.setLocation(new Location(nextX, nextY));
    }

    /**
     * Zeichnet die Plattform auf dem übergebenen Pane und legt alle Bewegungsparameter fest.
     *
     * @param pane   Wurzelelement der Szene, auf dem die Plattform erscheinen soll.
     * @param x      Startposition auf der X-Achse (gleichzeitig linkes Limit).
     * @param y      Startposition auf der Y-Achse (Höhe der Plattform).
     * @param travel Distanz in Pixeln, die nach rechts zurückgelegt werden darf.
     * @param speed  Bewegungsgeschwindigkeit pro Update in Pixeln.
     * @return Referenz auf die Plattform für fluente Aufrufe.
     */
    public EntityMovingPlatform drawPlatform(Pane pane, double x, double y, double travel, double speed) {
        // Grafische Ressource der Plattform laden. Die Datei liegt im selben Asset-Verzeichnis wie der Spieler.
        Image image = new Image(getClass().getResource("/assets/platform.png").toExternalForm());

        // ImageView erzeugen und auf die gewünschte Größe skalieren.
        this.sprite = new ImageView(image);
        this.sprite.setFitWidth(80);
        this.sprite.setFitHeight(20);

        // Das Sprite dem Pane hinzufügen, damit es gezeichnet wird.
        pane.getChildren().add(this.sprite);

        // Bewegungsparameter speichern: linke und rechte Grenze, Geschwindigkeit, Basis-Y.
        this.minX = x;
        this.maxX = x + Math.max(0, travel);
        this.speed = Math.max(0, speed);
        this.baseY = y;

        // Startposition auf dem Pane setzen.
        this.sprite.setX(x);
        this.sprite.setY(y);

        // Bounding-Box des Entities initialisieren (für Kollisionsabfragen).
        this.setWidth((float) this.sprite.getFitWidth());
        this.setHeight((float) this.sprite.getFitHeight());
        this.setLocation(new Location(x, y));

        return this;
    }

    /**
     * Prüft, ob ein Spieler gerade auf der Plattform steht bzw. landet.
     *
     * <p>Es reicht eine einfache AABB-Kollisionserkennung (Axis-Aligned Bounding Box). Wir achten
     * darauf, dass der Spieler von oben auftrifft und sein unterer Rand in der Nähe der Plattform-Oberseite liegt.</p>
     *
     * @param player Spieler-Entität, die geprüft werden soll.
     * @return {@code true}, wenn der Spieler auf der Plattform steht oder frisch landet.
     */
    public boolean supportsPlayer(EntityPlayer player) {
        // Ohne Plattform- oder Spieler-Position gibt es nichts zu prüfen.
        if (player == null || player.getLocation() == null || this.getLocation() == null) {
            return false;
        }

        // Spieler-Bounding-Box aufbereiten.
        double playerLeft = player.getLocation().getX();
        double playerRight = playerLeft + player.getWidth();
        double playerTop = player.getLocation().getY();
        double playerBottom = playerTop + player.getHeight();

        // Plattform-Bounding-Box aufbereiten.
        double platformLeft = this.getLocation().getX();
        double platformRight = platformLeft + this.getWidth();
        double platformTop = this.getLocation().getY();
        double platformBottom = platformTop + this.getHeight();

        // Horizontal muss sich der Spieler zumindest teilweise über der Plattform befinden.
        boolean horizontallyAligned = playerRight > platformLeft && playerLeft < platformRight;

        // Vertikal prüfen wir, ob der Spieler gerade von oben kommt und die Oberkante berührt.
        boolean isLandingFromAbove = playerBottom >= platformTop && playerTop < platformTop;

        // Kleiner Toleranzbereich erlaubt, damit leichte Überschneidungen nicht durchrutschen.
        boolean withinVerticalTolerance = playerBottom <= platformBottom + 4;

        return horizontallyAligned && isLandingFromAbove && withinVerticalTolerance;
    }

    /**
     * Setzt den Spieler exakt auf die Plattformoberkante, sobald {@link #supportsPlayer(EntityPlayer)} positiv ist.
     *
     * @param player Spieler, dessen Position korrigiert werden soll.
     */
    public void resolvePlayerLanding(EntityPlayer player) {
        // Nur wenn eine Kollision erkannt wurde, führen wir eine Korrektur durch.
        if (!supportsPlayer(player)) {
            return;
        }

        // Die neue Y-Position liegt genau auf der Oberseite der Plattform.
        double platformTop = this.getLocation().getY();
        double correctedY = platformTop - player.getHeight();

        // X-Position des Spielers bleibt unangetastet, damit das Momentum erhalten bleibt.
        double playerX = player.getLocation().getX();

        // Location aktualisieren, sodass der Spieler visuell und logisch auf der Plattform steht.
        player.setLocation(new Location(playerX, correctedY));
    }
}