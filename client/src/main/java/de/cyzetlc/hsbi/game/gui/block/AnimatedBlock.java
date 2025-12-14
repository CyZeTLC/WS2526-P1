package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Die abstrakte Klasse {@code AnimatedBlock} erweitert {@code Block} und bietet die Grundfunktionalität
 * zur Verwaltung und Anzeige einer Bildsequenz (Animation).
 * <p>
 * Sie kümmert sich um das Laden der Frames, die Zeitsteuerung der Animation und das Aktualisieren
 * des angezeigten Sprites basierend auf der verstrichenen Zeit.
 *
 * @see Block
 *
 * @author Tom Coombs
 */
public abstract class AnimatedBlock extends Block {
    /**
     * Ein Array von Pfaden zu den Bilddateien, die die Animations-Frames bilden.
     */
    protected final String[] FRAME_PATHS;

    /**
     * Die Dauer (in Sekunden), die jeder Frame angezeigt wird, bevor zum nächsten gewechselt wird.
     */
    private final double FRAME_DURATION_SECONDS = 0.6;

    /**
     * Liste der geladenen {@code Image}-Frames.
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
     * Der Zeitstempel des letzten Updates in Nanosekunden, zur Berechnung der Delta-Zeit.
     */
    private long lastFrameTimeNanos = 0L;

    /**
     * Konstruiert einen neuen {@code AnimatedBlock}.
     *
     * @param location Die Weltposition des Blocks.
     * @param frame_paths Ein Array von String-Pfaden zu den Animations-Frames.
     */
    public AnimatedBlock(Location location, String[] frame_paths) {
        super(location);
        this.FRAME_PATHS = frame_paths;
    }

    /**
     * Zeichnet den Block auf das {@code Pane} und stellt sicher, dass die Animations-Frames
     * geladen werden, bevor sie angezeigt werden können.
     *
     * @param pane Das {@code Pane}, auf dem der Block gezeichnet wird.
     */
    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.loadFrames();
    }

    /**
     * Aktualisiert den Zustand des Blocks.
     * <p>
     * Ruft die Animationsfortschrittslogik auf und setzt den Sprite auf den aktuellen Frame.
     */
    @Override
    public void update() {
        super.update();
        this.advanceAnimation();
        if (!this.frames.isEmpty()) {
            this.sprite.setImage(this.frames.get(this.currentFrame));
        }
    }

    /**
     * Lädt die Bilder für alle in {@code FRAME_PATHS} definierten Animations-Frames.
     * <p>
     * Bei Fehlern wird ein Fallback auf eine Standard-Tileset-Textur verwendet, um Abstürze zu vermeiden.
     */
    private void loadFrames() {
        if (!this.frames.isEmpty()) {
            return;
        }
        for (String path : FRAME_PATHS) {
            try {
                Image image = new Image(getClass().getResource(path).toExternalForm());
                this.frames.add(image);
            } catch (Exception ignored) { }
        }
        if (this.frames.isEmpty()) {
            // Fallback: alter Tileset, falls Ressourcen fehlen
            Image fallback = new Image("assets/tileset/sandstone_tiles.png");
            this.frames.add(fallback);
        }
    }

    /**
     * Bewegt die Animation basierend auf der seit dem letzten Update verstrichenen Zeit weiter.
     * <p>
     * Berechnet die Delta-Zeit und wechselt zum nächsten Frame, wenn die {@code FRAME_DURATION_SECONDS}
     * überschritten wurde.
     */
    private void advanceAnimation() {
        if (this.frames.size() < 2) {
            return;
        }
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
            this.currentFrame = (this.currentFrame + 1) % this.frames.size();
        }
    }
}