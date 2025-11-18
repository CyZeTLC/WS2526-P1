package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public abstract class AnimatedBlock extends Block {
    protected final String[] FRAME_PATHS;
    private final double FRAME_DURATION_SECONDS = 0.6;

    private final List<Image> frames = new ArrayList<>();
    private int currentFrame = 0;
    private double frameTimer = 0;
    private long lastFrameTimeNanos = 0L;

    public AnimatedBlock(Location location, String[] frame_paths) {
        super(location);
        this.FRAME_PATHS = frame_paths;
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.loadFrames();
    }

    @Override
    public void update() {
        super.update();
        this.advanceAnimation();
        this.sprite.setImage(this.frames.get(this.currentFrame));
    }

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
