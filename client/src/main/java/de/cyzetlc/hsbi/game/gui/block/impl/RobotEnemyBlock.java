package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;
import lombok.Getter;

/**
 * Der {@code RobotEnemyBlock} repräsentiert eine persistente, patrouillierende feindliche Entität (ein Roboter),
 * die sich horizontal innerhalb eines definierten Bereichs bewegt und periodisch Projektile auf den Spieler abfeuert.
 *
 * <p>
 * Dieser Block implementiert grundlegende KI-Logik, um die horizontale Position des Spielers innerhalb seines
 * Patrouillenbereichs zu verfolgen und einen Feuer-Cooldown zu verwalten.
 *
 * @see Block
 * @see LaserBlock
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class RobotEnemyBlock extends Block {
    /**
     * Die minimale X-Koordinate des horizontalen Patrouillenbereichs des Feindes.
     */
    protected final double minX;

    /**
     * Die maximale X-Koordinate des horizontalen Patrouillenbereichs des Feindes.
     */
    protected final double maxX;

    /**
     * Die feste Y-Koordinate des Feindes, die sicherstellt, dass er auf seiner Plattform geerdet bleibt.
     */
    protected double baseY;

    /**
     * Die konstante Geschwindigkeit, mit der sich der Roboter horizontal beim Patrouillieren oder Verfolgen bewegt (Einheiten pro Sekunde).
     */
    protected final double speed;

    /**
     * Flag, das anzeigt, ob der Feind besiegt wurde. Ein toter Feind ist inaktiv und nicht kollidierbar.
     */
    @Getter
    protected boolean dead = false;

    /**
     * Der Zeitstempel des letzten Update-Zyklus in Nanosekunden, der zur Berechnung der Delta-Zeit für Bewegung und Timer verwendet wird.
     */
    private long lastUpdateNanos = 0L;

    /**
     * Akkumulator für die seit dem letzten Abfeuern eines Projektils verstrichene Zeit.
     */
    protected double fireTimer = 0;

    /**
     * Die minimal erforderliche Zeit (in Sekunden) zwischen dem Abfeuern von Projektilen.
     */
    protected final double fireCooldown = 1.2;

    /**
     * Zeitstempel (in Sekunden) des letzten erfolgreichen Treffers des Spielers durch diesen Feind.
     * Wird verwendet, um die Unverwundbarkeits-/Cooldown-Zeit des Spielers zu implementieren.
     */
    private double lastHitTime = -1;

    /**
     * Konstruiert eine neue {@code RobotEnemyBlock}-Instanz.
     *
     * @param location Die anfängliche Weltposition (obere linke Ecke).
     * @param patrolWidth Die Gesamtbreite des Bereichs, den der Roboter patrouillieren soll (gemessen von der anfänglichen X-Koordinate).
     * @param speed Die horizontale Bewegungsgeschwindigkeit.
     */
    public RobotEnemyBlock(Location location, double patrolWidth, double speed) {
        super(location);
        this.setMaterial(Material.ROBOT_ENEMY);
        this.setCollideAble(true);
        this.setWidth(48);
        this.setHeight(96);
        this.minX = location.getX();
        this.maxX = location.getX() + Math.max(0, patrolWidth);
        this.baseY = location.getY();
        this.speed = speed;
    }

    /**
     * Zeichnet den Roboterfeind, stellt sicher, dass seine visuellen Grenzen mit seinen Kollisionsgrenzen übereinstimmen
     * und behält seine feste vertikale Position bei. Bringt auch den Sprite nach vorne.
     *
     * @param pane Das {@code Pane}, auf dem der Block gezeichnet wird.
     */
    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.setWidth((float) this.sprite.getBoundsInLocal().getWidth());
        this.setHeight((float) this.sprite.getBoundsInLocal().getHeight());
        this.getLocation().setY(this.baseY);
        if (this.sprite.getParent() != null) {
            this.sprite.toFront();
        }
    }

    /**
     * Platzhalter für die Kollisionslogik. Die eigentliche Kollisionsverarbeitung (Prüfung, ob der
     * Spieler auf den Feind springt) wird oft zentral im {@code GameScreen} oder in der Level-Update-Schleife
     * behandelt, um eine doppelte Verarbeitung zu vermeiden.
     *
     * @param player Die {@code Player}-Entität, die mit dem Feind kollidiert ist.
     */
    @Override
    public void onCollide(de.cyzetlc.hsbi.game.entity.Player player) {
        // wird im GameScreen behandelt, um doppelte Verarbeitung zu vermeiden
    }

    /**
     * Markiert den Feind als tot, deaktiviert den Block und deaktiviert Kollisionen.
     */
    public void kill() {
        this.dead = true;
        this.setActive(false);
        this.setCollideAble(false);
    }

    /**
     * Aktualisiert die Bewegung und die internen Timer des Roboters.
     * <p>
     * Berechnet die Delta-Zeit, bestimmt eine Ziel-X-Position (begrenzt auf den Patrouillenbereich,
     * den Spieler verfolgend), aktualisiert die Position des Roboters basierend auf der Geschwindigkeit
     * und aktualisiert die Blickrichtung.
     */
    @Override
    public void update() {
        super.update();
        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
            return;
        }
        double delta = (now - lastUpdateNanos) / 1_000_000_000.0;
        lastUpdateNanos = now;

        if (dead) return;

        double targetX = Game.thePlayer != null ? Game.thePlayer.getLocation().getX() : this.getLocation().getX();
        double clampedTarget = Math.max(minX, Math.min(maxX - this.getWidth(), targetX));

        double currentX = this.getLocation().getX();
        double dx = clampedTarget - currentX;
        double step = Math.signum(dx) * speed * delta;
        if (Math.abs(step) > Math.abs(dx)) {
            step = dx;
        }
        double nextX = currentX + step;

        this.getLocation().setX(nextX);
        this.getLocation().setY(baseY);

        this.updateFacingToPlayer();

        fireTimer += delta;
    }

    /**
     * Versucht, ein neues {@code LaserBlock}-Projektil zu erstellen und zurückzugeben, wenn der Feind
     * nicht tot ist, der Spieler sich in einem engen horizontalen Bereich befindet und der
     * Feuer-Cooldown abgelaufen ist.
     * <p>
     * Wenn ein Laser erstellt wird, wird der {@code fireTimer} zurückgesetzt. Der Laser wird auf
     * ungefährer "Augenhöhe" des Roboters gespawnt.
     *
     * @param player Die Ziel-Spieler-Entität.
     * @return Ein neuer {@code LaserBlock}, wenn bereit zum Feuern, ansonsten {@code null}.
     */
    public LaserBlock tryFire(EntityPlayer player) {
        if (dead || player == null) return null;

        // Spieler muss deutlich oberhalb sein (Sprung ueber den Boss)
        //boolean playerAbove = player.getLocation().getY() + player.getHeight() < this.getLocation().getY();
        boolean closeHorizontally = Math.abs(player.getLocation().getX() - this.getLocation().getX()) < 440;
        if (!closeHorizontally) {
            return null;
        }
        if (fireTimer < fireCooldown) {
            return null;
        }
        fireTimer = 0;

        int dir = player.getLocation().getX() >= this.getLocation().getX() ? 1 : -1;
        double eyeY = this.getLocation().getY() + this.getHeight() * 0.35;
        double spawnX = dir == 1 ? this.getLocation().getX() + this.getWidth() - 4 : this.getLocation().getX() - 8;

        return new LaserBlock(new Location(spawnX, eyeY), dir, 320);
    }

    /**
     * Verarbeitet einen Treffer auf den Spieler durch diesen Roboter.
     * <p>
     * Reduziert die Gesundheit des Spielers um 1 Punkt, wenn der Spieler nicht unverwundbar ist
     * und der interne Cooldown des Roboters abgelaufen ist.
     *
     * @param player Die {@code Player}-Entität, die getroffen wurde.
     */
    public void hitPlayer(de.cyzetlc.hsbi.game.entity.Player player) {
        if (dead) return;
        // Treffer ignorieren, wenn der Spieler unverwundbar ist.
        if (player.isGodModeEnabled()) {
            return;
        }
        double now = System.nanoTime() / 1e9;
        if (this.lastHitTime < 0 || now - this.lastHitTime > 0.5) {
            player.setHealth(player.getHealth() - 1f);
            this.lastHitTime = now;
        }
    }

    /**
     * Aktualisiert die Blickrichtung (horizontaler Spiegelzustand) des Roboters, sodass er
     * dem Spieler zugewandt ist.
     */
    private void updateFacingToPlayer() {
        if (this.sprite == null) {
            return;
        }
        double playerX = Game.thePlayer != null ? Game.thePlayer.getLocation().getX() : this.getLocation().getX();
        boolean faceRight = playerX >= this.getLocation().getX();
        this.sprite.setScaleX(faceRight ? 1 : -1);
        this.sprite.toFront();
    }
}