package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

/**
 * Der {@code LaserBlock} repräsentiert ein von einem Feind abgefeuertes Projektil, das
 * horizontal fliegt und bei Kollision mit dem Spieler Schaden zufügt.
 * <p>
 * Dieser Block hat eine begrenzte Lebensdauer (4,0 Sekunden) und verschwindet automatisch
 * bei Aufprall oder Ablauf der Zeit. Die Bewegung wird basierend auf der zwischen den
 * Frames verstrichenen Zeit (`delta`) berechnet.
 *
 * @see Block
 * @see Material#ROBOT_LASER
 *
 * @author Leonardo Parrino
 */
public class LaserBlock extends Block {
    /**
     * Die konstante Geschwindigkeit, mit der das Laserprojektil fliegt (Einheiten pro Sekunde).
     */
    private final double speed;

    /**
     * Die horizontale Bewegungsrichtung: 1 für rechts, -1 für links.
     */
    private final int direction;

    /**
     * Verfolgt die Gesamtzeit (in Sekunden), die der Laser seit seiner Erstellung aktiv war.
     */
    private double lifeSeconds = 0;

    /**
     * Der Zeitstempel des letzten Update-Zyklus in Nanosekunden, der zur Berechnung der Delta-Zeit verwendet wird.
     */
    private long lastUpdateNanos = 0L;

    /**
     * Konstruiert eine neue {@code LaserBlock}-Instanz und initialisiert deren Bewegungseigenschaften und Aussehen.
     *
     * @param location Die anfängliche Weltposition des Lasers.
     * @param direction Die anfängliche Bewegungsrichtung (jeder nicht-negative Wert ist rechts, negativ ist links).
     * @param speed Die konstante Bewegungsgeschwindigkeit des Lasers.
     */
    public LaserBlock(Location location, int direction, double speed) {
        super(location);
        this.setMaterial(Material.ROBOT_LASER);
        this.setCollideAble(false);
        this.speed = speed;
        this.direction = direction >= 0 ? 1 : -1;
        this.setWidth(12);
        this.setHeight(12);
    }

    /**
     * Überschreibt die Standard-Zeichnenmethode, um die visuelle Größe des Laser-Sprites anzupassen,
     * während die Kollisions-Box-Größe konstant bleibt.
     *
     * @param pane Das {@code Pane}, auf dem der Laser gezeichnet wird.
     */
    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.sprite.setPreserveRatio(true);
        this.sprite.setFitWidth(24);
        this.sprite.setFitHeight(24);
    }

    /**
     * Behandelt die Kollisionslogik, wenn der Laser einen Spieler trifft.
     * <p>
     * Wenn sich der Spieler im God Mode befindet, wird der Laser einfach konsumiert und verschwindet.
     * Andernfalls erleidet der Spieler 1 Schadenspunkt, und der Laser wird deaktiviert.
     *
     * @param player Die {@code Player}-Entität, die mit dem Laser kollidiert ist.
     */
    @Override
    public void onCollide(de.cyzetlc.hsbi.game.entity.Player player) {
        if (!this.isActive()) return;
        // GodMode frisst den Treffer, aber der Laser verschwindet trotzdem.
        if (player.isGodModeEnabled()) {
            this.setActive(false);
            return;
        }
        player.setHealth(player.getHealth() - 1f);
        this.setActive(false);
    }

    /**
     * Aktualisiert die Position und Lebensdauer des Lasers basierend auf der seit dem letzten Frame
     * verstrichenen Zeit.
     * <p>
     * Berechnet die Delta-Zeit (`delta`), prüft, ob die Lebensdauer des Lasers 4,0 Sekunden überschreitet,
     * und aktualisiert die horizontale Position basierend auf Geschwindigkeit und Richtung.
     */
    @Override
    public void update() {
        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
            return;
        }
        double delta = (now - lastUpdateNanos) / 1_000_000_000.0;
        lastUpdateNanos = now;

        lifeSeconds += delta;
        if (lifeSeconds > 4.0) {
            this.setActive(false);
            return;
        }

        double dx = direction * speed * delta;
        this.getLocation().setX(this.getLocation().getX() + dx);

        super.update();
    }
}