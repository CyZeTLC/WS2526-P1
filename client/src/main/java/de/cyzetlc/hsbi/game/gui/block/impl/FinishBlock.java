package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class FinishBlock extends Block {
    private static final String[] FRAME_PATHS = {
            "/assets/tileset/finish/finish_first.png",
            "/assets/tileset/finish/finish_second.png"
    };
    private static final double FRAME_DURATION_SECONDS = 0.6;

    private final List<Image> frames = new ArrayList<>();
    private int currentFrame = 0;
    private double frameTimer = 0;
    private long lastFrameTimeNanos = 0L;

    public FinishBlock(Location location) {
        super(location);
        this.setMaterial(Material.SERVER);
        this.setCollideAble(false);
        this.setWidth(45);
        this.setHeight(90);
    }

    @Override
    public void onCollide(Player player) {
        player.setHealth(0.0F);
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
