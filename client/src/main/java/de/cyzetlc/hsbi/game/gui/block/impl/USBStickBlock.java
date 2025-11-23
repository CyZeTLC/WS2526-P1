package de.cyzetlc.hsbi.game.gui.block.impl;

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

public class USBStickBlock extends PerkBlock {
    private static final double FRAME_DURATION_SECONDS = 0.6;
    private static final String[] FRAME_PATHS = new String[] {
            "/assets/USB-Stick/USB-Stick-Raw.png",
            "/assets/USB-Stick/USB-Stick-Gelb.png",
            "/assets/USB-Stick/USB-Stick-Gr\u00fcn.png",
            "/assets/USB-Stick/USB-Stick-Rot.png",
            "/assets/USB-Stick/USB-Stick-Totenkopf.png"
    };

    private final List<Image> frames = new ArrayList<>();
    private int currentFrame = 0;
    private double frameTimer = 0;
    private long lastAnimationFrameNanos = 0L;

    private boolean triggered = false;

    public USBStickBlock(Location location) {
        super(location);
        this.setMaterial(Material.USB_STICK);
        this.setCollideAble(false);
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.loadFrames();
        if (!this.frames.isEmpty()) {
            this.getSprite().setImage(this.frames.get(0));
        }
    }

    @Override
    public void update() {
        super.update();
        this.advanceAnimation();
    }

    @Override
    public void onCollide(Player player) {
        if (this.triggered) {
            return;
        }
        this.triggered = true;
        this.setActive(false);
        SoundManager.playWithDuck(Sound.USB_STICK, 1.0, 0.06);
    }

    private void loadFrames() {
        if (!this.frames.isEmpty()) {
            return;
        }
        for (String path : FRAME_PATHS) {
            try {
                Image image = new Image(getClass().getResource(path).toExternalForm());
                this.frames.add(image);
            } catch (Exception ignored) {
                // keep trying other frames
            }
        }
        if (this.frames.isEmpty()) {
            try {
                Image fallback = new Image(getClass().getResource(this.getMaterial().texturePath).toExternalForm());
                this.frames.add(fallback);
            } catch (Exception ignored) { }
        }
    }

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
