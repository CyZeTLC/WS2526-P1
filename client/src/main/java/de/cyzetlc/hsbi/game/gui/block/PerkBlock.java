package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.world.Location;

/**
 * Die abstrakte Klasse {@code PerkBlock} erweitert {@code Block} und dient als Basis für alle
 * einsammelbaren Objekte (Power-ups) im Spiel.
 * <p>
 * {@code PerkBlock}s implementieren eine standardmäßige vertikale Schweb-Animation, um ihre
 * Sichtbarkeit und Attraktivität für den Spieler zu erhöhen.
 *
 * @see Block
 *
 * @author Tom Coombs
 */
public abstract class PerkBlock extends Block {
    /**
     * Die Dauer (in Sekunden), die der Block in einer Richtung (hoch oder runter) schwebt,
     * bevor die Bewegungsrichtung umgekehrt wird.
     */
    private final double FRAME_DURATION_SECONDS = 0.6;

    /**
     * Zähler für die Zeit, die seit dem letzten Richtungswechsel vergangen ist.
     */
    private double frameTimer = 0;
    /**
     * Der Zeitstempel des letzten Updates in Nanosekunden, zur Berechnung der Delta-Zeit.
     */
    private long lastFrameTimeNanos = 0L;
    /**
     * Flag, das die aktuelle Schweb-Richtung speichert (true = aufwärts, false = abwärts/zurück).
     */
    private boolean upDown = false;

    /**
     * Konstruiert einen neuen {@code PerkBlock} an der angegebenen Position.
     *
     * @param location Die Weltposition des Blocks.
     */
    public PerkBlock(Location location) {
        super(location);
    }

    /**
     * Aktualisiert den Zustand des Blocks, einschließlich der vertikalen Schweb-Animation.
     * <p>
     * Nach Ablauf der {@code FRAME_DURATION_SECONDS} wird die vertikale Position um 5 Einheiten
     * verschoben und die Bewegungsrichtung umgekehrt.
     */
    @Override
    public void update() {
        super.update();

        long now = System.nanoTime();
        if (this.lastFrameTimeNanos == 0L) {
            this.lastFrameTimeNanos = now;
            return;
        }
        double deltaSeconds = (now - this.lastFrameTimeNanos) / 1_000_000_000.0;
        this.lastFrameTimeNanos = now;
        this.frameTimer += deltaSeconds;
        if (this.frameTimer >= FRAME_DURATION_SECONDS) {
            this.frameTimer -= FRAME_DURATION_SECONDS;
            // Bestimmt die Schweb-Richtung und passt die Y-Koordinate an (±5 Pixel)
            if (this.upDown) {
                this.getLocation().setY(this.getLocation().getY() + 5);
                this.upDown = false; // Nächster Zustand: Hoch
            } else {
                this.getLocation().setY(this.getLocation().getY() - 5);
                this.upDown = true; // Nächster Zustand: Runter
            }
        }
    }
}