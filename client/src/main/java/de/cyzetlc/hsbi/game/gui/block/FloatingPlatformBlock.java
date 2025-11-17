package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Eine schwebende Plattform, die sich zwischen zwei Wegpunkten hin und her bewegt.
 * <p>
 * Die Felder {@link #startLocation}, {@link #endLocation} und {@link #speed} bestimmen Reichweite
 * und Tempo. {@link #movingTowardsEnd} merkt sich, welche Richtung im aktuellen Tick aktiv ist.
 */
public class FloatingPlatformBlock extends Block {

    private static final double FRAME_DURATION_SECONDS = 0.6;

    private final List<Image> frames = new ArrayList<>();
    private int currentFrame = 0;
    private double frameTimer = 0;

    private final Location startLocation;
    private final Location endLocation;
    private final double speed;
    private final int skinIndex;
    private boolean movingTowardsEnd = true;

    /**
     * Erstellt eine Plattform, die zwischen zwei Punkten pendelt.
     *
     * @param startLocation Ausgangspunkt der Bewegung (auch Spawn-Position).
     * @param endLocation   Zielpunkt, zu dem die Plattform pendelt.
     * @param speed         Bewegungsgeschwindigkeit in Pixeln pro Sekunde.
     */
    public FloatingPlatformBlock(Location startLocation, Location endLocation, double speed) {
        this(startLocation, endLocation, speed, 1);
    }

    /**
     * @param skinIndex Auswahlnummer (1-3) fuer die gewuenschte 2-Frame-Server-Optik.
     */
    public FloatingPlatformBlock(Location startLocation, Location endLocation, double speed, int skinIndex) {
        super(new Location(startLocation.getX(), startLocation.getY()));
        this.startLocation = copy(startLocation);
        this.endLocation = copy(endLocation);
        this.speed = Math.max(5, speed);
        this.skinIndex = Math.max(1, Math.min(3, skinIndex));

        this.setMaterial(Material.FLOATING_PLATFORM); // Default (Frame 1)
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
        if (this.getLastUpdateNanos() == 0L) {
            this.setLastUpdateNanos(now);
            return;
        }

        double deltaSeconds = (now - this.getLastUpdateNanos()) / 1_000_000_000.0;
        this.setLastUpdateNanos(now);
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

        this.advanceAnimation(deltaSeconds);
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.loadFrames();
        if (!frames.isEmpty()) {
            this.getSprite().setImage(frames.get(0));
        }
    }

    private static Location copy(Location location) {
        return new Location(location.getX(), location.getY());
    }

    private void loadFrames() {
        if (!this.frames.isEmpty()) {
            return;
        }

        for (String framePath : resolveFramePaths(this.skinIndex)) {
            try {
                Image image = new Image(getClass().getResource(framePath).toExternalForm());
                this.frames.add(image);
            } catch (Exception ignored) {
                // Ignoriere fehlende Frames, wir springen auf Fallback.
            }
        }

        if (this.frames.isEmpty()) {
            try {
                Image fallback = new Image(getClass().getResource(Material.FLOATING_PLATFORM.texturePath).toExternalForm());
                this.frames.add(fallback);
            } catch (Exception ignored) { }
        }
    }

    private void advanceAnimation(double deltaSeconds) {
        if (this.frames.size() < 2) {
            return;
        }

        this.frameTimer += deltaSeconds;
        if (this.frameTimer >= FRAME_DURATION_SECONDS) {
            this.frameTimer -= FRAME_DURATION_SECONDS;
            this.currentFrame = (this.currentFrame + 1) % this.frames.size();
            this.getSprite().setImage(this.frames.get(this.currentFrame));
        }
    }

    /**
     * Liefert die beiden Frame-Pfade fuer den gewuenschten Skin.
     */
    private static String[] resolveFramePaths(int skinIndex) {
        return switch (skinIndex) {
            case 2 -> new String[]{
                    "/assets/movingplatform/zuschnitt2.png",
                    "/assets/movingplatform/zuschnitt2_zustand1.png"
            };
            case 3 -> new String[]{
                    "/assets/movingplatform/zuschnitt3.png",
                    "/assets/movingplatform/zuschnitt3_zustand1.png"
            };
            default -> new String[]{
                    "/assets/movingplatform/zuschnitt1.png",
                    "/assets/movingplatform/zuschnitt1_zustand1.png"
            };
        };
    }

    private long getLastUpdateNanos() {
        return this.lastUpdateNanos;
    }

    private void setLastUpdateNanos(long lastUpdateNanos) {
        this.lastUpdateNanos = lastUpdateNanos;
    }

    // getrenntes Feld behalten, da wir Getter/Setter fuer Animation brauchen
    private long lastUpdateNanos = 0L;
}
