package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Der {@code FloatingPlatformBlock} repräsentiert eine schwebende Plattform, die sich
 * zwischen zwei Wegpunkten hin und her bewegt.
 * <p>
 * Die Felder {@link #startLocation}, {@link #endLocation} und {@link #speed} bestimmen
 * Reichweite und Tempo. {@link #movingTowardsEnd} merkt sich, welche Richtung im aktuellen
 * Tick aktiv ist.
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class FloatingPlatformBlock extends Block {

    /**
     * Die Dauer (in Sekunden) jedes Frames der Plattformanimation.
     */
    private static final double FRAME_DURATION_SECONDS = 0.6;

    /**
     * Liste der {@code Image}-Frames für die Animation der Plattform.
     */
    private final List<Image> frames = new ArrayList<>();
    /**
     * Index des aktuell angezeigten Animations-Frames.
     */
    private int currentFrame = 0;
    /**
     * Zähler für die Zeit, die seit dem letzten Frame-Wechsel vergangen ist.
     */
    private double frameTimer = 0;

    /**
     * Die Startposition der Plattform.
     */
    private final Location startLocation;
    /**
     * Die Zielposition, zu der die Plattform pendelt.
     */
    private final Location endLocation;
    /**
     * Die Bewegungsgeschwindigkeit in Pixeln pro Sekunde.
     */
    private final double speed;
    /**
     * Index zur Auswahl des visuellen Skins der Plattform (1 bis 3).
     */
    private final int skinIndex;
    /**
     * Flag, das angibt, ob sich die Plattform derzeit in Richtung der {@code endLocation} bewegt.
     */
    private boolean movingTowardsEnd = true;

    /**
     * Erstellt eine Plattform, die zwischen zwei Punkten pendelt, unter Verwendung des Standard-Skins (1).
     *
     * @param startLocation Ausgangspunkt der Bewegung (auch Spawn-Position).
     * @param endLocation   Zielpunkt, zu dem die Plattform pendelt.
     * @param speed         Bewegungsgeschwindigkeit in Pixeln pro Sekunde.
     */
    public FloatingPlatformBlock(Location startLocation, Location endLocation, double speed) {
        this(startLocation, endLocation, speed, 1);
    }

    /**
     * Erstellt eine Plattform, die zwischen zwei Punkten pendelt, unter Angabe eines bestimmten Skins.
     *
     * @param startLocation Ausgangspunkt der Bewegung (auch Spawn-Position).
     * @param endLocation   Zielpunkt, zu dem die Plattform pendelt.
     * @param speed         Bewegungsgeschwindigkeit in Pixeln pro Sekunde.
     * @param skinIndex Auswahlnummer (1-3) für die gewünschte 2-Frame-Server-Optik.
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

    /**
     * Behandelt die Kollision der Plattform mit dem Spieler.
     * <p>
     * Es ist keine spezielle Logik erforderlich, da die Standard-Kollisionsbehandlung im {@code Block}
     * den Spieler automatisch auf die Plattform stellt und deren Bewegung übernimmt.
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    @Override
    public void onCollide(Player player) {
        // Keine Speziallogik notwendig - die Standardkollision laesst den Spieler darauf stehen.
    }

    /**
     * Aktualisiert Position und Richtung der Plattform anhand der vergangenen Zeit.
     * <p>
     * Die Methode berechnet die zurückzulegende Strecke, aktualisiert die Position und die
     * Bildschirmposition (unter Berücksichtigung des Kamera-Offsets) und kehrt die Richtung um,
     * sobald ein Wegpunkt erreicht ist. Die Verschiebung (Delta) wird gespeichert, damit der
     * Spieler, der auf der Plattform steht, entsprechend mitbewegt werden kann.
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

            if (Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen) {
                this.getSprite().setX(target.getX() - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraX());
                this.getSprite().setY(target.getY() - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraY());
            }

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

        if (Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen) {
            this.getSprite().setX(nextX - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraX());
            this.getSprite().setY(nextY - ((GameScreen) Game.getInstance().getScreenManager().getCurrentScreen()).getCameraY());
        }

        this.setDelta(nextX - oldX, nextY - oldY);

        this.advanceAnimation(deltaSeconds);
    }

    /**
     * Zeichnet die Plattform und lädt die Animations-Frames, falls noch nicht geschehen.
     * <p>
     * Stellt sicher, dass das erste Frame der Animation als Startbild festgelegt wird.
     *
     * @param pane Das {@code Pane}, auf dem der Block gezeichnet wird.
     */
    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.loadFrames();
        if (!frames.isEmpty()) {
            this.getSprite().setImage(frames.get(0));
        }
    }

    /**
     * Erstellt eine tiefe Kopie der gegebenen {@code Location}.
     *
     * @param location Die zu kopierende {@code Location}.
     * @return Eine neue {@code Location}-Instanz mit denselben Koordinaten.
     */
    private static Location copy(Location location) {
        return new Location(location.getX(), location.getY());
    }

    /**
     * Lädt die Animations-Frames für den konfigurierten {@code skinIndex} aus den Ressourcen.
     * <p>
     * Bei fehlenden Frames wird ein Fallback auf die Standard-Textur des Materials verwendet.
     */
    private void loadFrames() {
        if (!this.frames.isEmpty()) {
            return;
        }

        // Skin 0 = alte, statische MovingPlatform
        if (this.skinIndex <= 0) {
            try {
                Image image = new Image(getClass().getResource(this.getMaterial().texturePath).toExternalForm());
                this.frames.add(image);
            } catch (Exception ignored) { }
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
                Image fallback = new Image(getClass().getResource(this.getMaterial().texturePath).toExternalForm());
                this.frames.add(fallback);
            } catch (Exception ignored) { }
        }
    }

    /**
     * Bewegt die Animation um die seit dem letzten Aufruf vergangene Zeit weiter.
     * <p>
     * Wechselt den Frame, wenn die {@code FRAME_DURATION_SECONDS} überschritten wurde.
     *
     * @param deltaSeconds Die seit dem letzten Update vergangene Zeit in Sekunden.
     */
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
     * Liefert die beiden Frame-Pfade für den gewünschten Skin.
     *
     * @param skinIndex Der Index des gewünschten Skins (1, 2 oder 3).
     * @return Ein String-Array mit den Pfaden zu Frame 1 und Frame 2.
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

    /**
     * Gibt den Zeitstempel (in Nanosekunden) der letzten Aktualisierung zurück.
     *
     * @return Der Zeitstempel des letzten Updates.
     */
    private long getLastUpdateNanos() {
        return this.lastUpdateNanos;
    }

    /**
     * Setzt den Zeitstempel (in Nanosekunden) der letzten Aktualisierung.
     *
     * @param lastUpdateNanos Der neue Zeitstempel.
     */
    private void setLastUpdateNanos(long lastUpdateNanos) {
        this.lastUpdateNanos = lastUpdateNanos;
    }

    /**
     * Das separate Feld zur Speicherung des Zeitstempels des letzten Updates für die Delta-Zeitberechnung.
     */
    private long lastUpdateNanos = 0L;
}