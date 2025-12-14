package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.block.PerkBlock;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Der {@code USBStickBlock} repräsentiert ein wichtiges, einsammelbares Objekt im Spiel (einen USB-Stick).
 * Beim Einsammeln wird die Fähigkeit des Spielers freigeschaltet, andere Dateien einzusammeln (z. B. {@code FolderBlock}).
 *
 * <p>
 * Dieser Block ist animiert und löst beim Einsammeln einen permanenten Zustandseffekt ({@code canCollectFiles = true}) aus.
 *
 * @see PerkBlock
 * @see FolderBlock
 *
 * @author Leonardo Parrino
 */
public class USBStickBlock extends PerkBlock {
    /**
     * Die Dauer (in Sekunden) jedes Animations-Frames.
     */
    private static final double FRAME_DURATION_SECONDS = 0.6;
    /**
     * Die Pfade zu den Animations-Frames des USB-Sticks (Rohzustand und verschiedene Lichtzustände).
     */
    private static final String[] FRAME_PATHS = new String[] {
            "/assets/USB-Stick/USB-Stick-Raw.png",
            "/assets/USB-Stick/USB-Stick-Gelb.png",
            "/assets/USB-Stick/USB-Stick-Gr\u00fcn.png",
            "/assets/USB-Stick/USB-Stick-Rot.png"
    };

    /**
     * Liste der geladenen {@code Image}-Frames für die Animation.
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
     * Der Zeitstempel des letzten Updates in Nanosekunden, zur Berechnung der Delta-Zeit für die Animation.
     */
    private long lastAnimationFrameNanos = 0L;

    /**
     * Flag, das angibt, ob der Effekt des USB-Sticks bereits ausgelöst wurde.
     */
    private boolean triggered = false;

    /**
     * Konstruiert einen neuen {@code USBStickBlock} an der angegebenen Position.
     * <p>
     * Initialisiert das Material, die Größe und setzt den Block als nicht kollidierbar.
     *
     * @param location Die Weltposition, an der der Block platziert werden soll.
     */
    public USBStickBlock(Location location) {
        super(location);
        this.setMaterial(Material.USB_STICK);
        this.setCollideAble(false);
        this.setWidth(28);
        this.setHeight(60);
    }

    /**
     * Zeichnet den Block und lädt die Animations-Frames, falls noch nicht geschehen.
     *
     * @param pane Das {@code Pane}, auf dem der Block gezeichnet wird.
     */
    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.loadFrames();
        if (!this.frames.isEmpty()) {
            this.getSprite().setImage(this.frames.get(0));
        }
    }

    /**
     * Aktualisiert den Zustand des Blocks, einschließlich der Animation.
     */
    @Override
    public void update() {
        super.update();
        this.advanceAnimation();
    }

    /**
     * Behandelt die Logik, die ausgeführt wird, wenn ein Spieler mit dem USB-Stick kollidiert.
     * <p>
     * Aktiviert die Fähigkeit des Spielers, Dateien zu sammeln, deaktiviert den Block und spielt einen Sound ab.
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    @Override
    public void onCollide(Player player) {
        if (this.triggered) {
            return;
        }
        this.triggered = true;
        this.setActive(false);
        Game.thePlayer.setCanCollectFiles(true);

        if (!Game.getInstance().getConfig().getObject().getBoolean("tutorialFinished")) {
            SoundManager.playWithDuck(Sound.USB_STICK, 1.0, 0.06);
        }
    }

    /**
     * Lädt die Animations-Frames aus den definierten Pfaden.
     * <p>
     * Führt einen Fallback auf die Standardtextur aus, falls keine Frames gefunden werden können.
     */
    private void loadFrames() {
        if (!this.frames.isEmpty()) {
            return;
        }
        for (String path : FRAME_PATHS) {
            try {
                Image image = new Image(getClass().getResource(path).toExternalForm());
                this.frames.add(image);
            } catch (Exception ignored) {
                // Versuche weiterhin, andere Frames zu laden
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
     */
    private void advanceAnimation() {
        if (this.frames.size() < 2 || this.getSprite() == null) {
            return;
        }
        long now = System.nanoTime();
        if (this.lastAnimationFrameNanos == 0L) {
            this.lastAnimationFrameNanos = now;
            return;
        }
        double deltaSeconds = (now - this.lastAnimationFrameNanos) / 1_000_000_000.0;
        this.lastAnimationFrameNanos = now;
        this.frameTimer += deltaSeconds;
        if (this.frameTimer >= FRAME_DURATION_SECONDS) {
            this.frameTimer -= FRAME_DURATION_SECONDS;
            this.currentFrame = (this.currentFrame + 1) % this.frames.size();
            this.getSprite().setImage(this.frames.get(this.currentFrame));
        }
    }
}