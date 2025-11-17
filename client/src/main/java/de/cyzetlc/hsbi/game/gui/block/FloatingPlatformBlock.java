package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.world.Location;

/**
 * Eine schwebende Plattform, die sich zwischen zwei Wegpunkten hin und her bewegt.
 * <p>
 * Die Felder {@link #startLocation}, {@link #endLocation} und {@link #speed} bestimmen Reichweite
 * und Tempo. {@link #movingTowardsEnd} merkt sich, welche Richtung im aktuellen Tick aktiv ist.
 */
public class FloatingPlatformBlock extends Block {

    private final Location startLocation;
    private final Location endLocation;
    private final double speed;
    private boolean movingTowardsEnd = true;
    private long lastUpdateNanos = 0L;

    /**
     * Erstellt eine Plattform, die zwischen zwei Punkten pendelt.
     *
     * @param startLocation Ausgangspunkt der Bewegung (auch Spawn-Position).
     * @param endLocation   Zielpunkt, zu dem die Plattform pendelt.
     * @param speed         Bewegungsgeschwindigkeit in Pixeln pro Sekunde.
     */
    public FloatingPlatformBlock(Location startLocation, Location endLocation, double speed) {
        super(new Location(startLocation.getX(), startLocation.getY()));
        this.startLocation = copy(startLocation);
        this.endLocation = copy(endLocation);
        this.speed = Math.max(5, speed);

        this.setMaterial(Material.FLOATING_PLATFORM);
        this.setCollideAble(true);
        this.setWidth(64);
        this.setHeight(84);
    }

    @Override
    public void onCollide(Player player) {
        // Keine Speziallogik notwendig - die Standardkollision laesst den Spieler darauf stehen.
    }

    /**
     * Aktualisiert Position und Richtung der Plattform anhand der vergangenen Zeit.
     */
    @Override
    public void update() {
        super.update();
        this.setDelta(0, 0); // Ruecksetzen, falls sich die Plattform in diesem Tick nicht bewegt

        if (!this.isActive() || this.getSprite() == null) {
            return;
        }

        long now = System.nanoTime();
        if (this.lastUpdateNanos == 0L) {
            this.lastUpdateNanos = now;
            return;
        }

        double deltaSeconds = (now - this.lastUpdateNanos) / 1_000_000_000.0;
        this.lastUpdateNanos = now;

        Location current = this.getLocation();
        double oldX = current.getX();
        double oldY = current.getY();
        Location target = this.movingTowardsEnd ? this.endLocation : this.startLocation;

        double dx = target.getX() - current.getX();
        double dy = target.getY() - current.getY();
        double distance = Math.hypot(dx, dy);

        if (distance < 0.5) {
            // Wegpunkt erreicht -> Richtung invertieren
            this.movingTowardsEnd = !this.movingTowardsEnd;
            current.setX(target.getX());
            current.setY(target.getY());
            this.getSprite().setX(target.getX() - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraX());
            this.getSprite().setY(target.getY());
            this.setDelta(target.getX() - oldX, target.getY() - oldY);
            return;
        }

        double travel = this.speed * deltaSeconds;
        double ratio = Math.min(1.0, travel / distance);

        double nextX = current.getX() + dx * ratio;
        double nextY = current.getY() + dy * ratio;

        if (ratio >= 1.0) {
            // Richtung umkehren, sobald die volle Strecke zurueckgelegt wurde.
            this.movingTowardsEnd = !this.movingTowardsEnd;
        }

        current.setX(nextX);
        current.setY(nextY);
        this.getSprite().setX(nextX - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraX());
        this.getSprite().setY(nextY);
        this.setDelta(nextX - oldX, nextY - oldY);
    }

    private static Location copy(Location location) {
        return new Location(location.getX(), location.getY());
    }
}
