package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class LavaBlock extends Block {
    private static final String[] FRAME_PATHS = {
            "/assets/lavaset/1Lava32x64.png",
            "/assets/lavaset/2Lava32x64.png",
            "/assets/lavaset/3Lava32x64.png"
    };
    private static final double FRAME_DURATION_SECONDS = 0.2;

    private final List<Image> frames = new ArrayList<>();
    private int currentFrame = 0;
    private double frameTimer = 0;
    private long lastFrameTimeNanos = 0L;

    public LavaBlock(Location location) {
        super(location);
        this.setMaterial(Material.LAVA);
        this.loadFrames();
    }

    @Override
    public void onCollide(Player player) {
        float newHealth = Math.max(0, player.getHealth() - 0.25f); // 25% Schaden pro Tick
        player.setHealth(newHealth);
        if (newHealth <= 0 && Game.getInstance() != null) {
            Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
        }
    }

    @Override
    public void update() {
        super.update();
        if (!this.isActive() || this.getSprite() == null || this.frames.isEmpty()) {
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
            this.getSprite().setImage(this.frames.get(this.currentFrame));
        }
    }

    private void loadFrames() {
        for (String path : FRAME_PATHS) {
            Image image = new Image(getClass().getResource(path).toExternalForm());
            this.frames.add(image);
        }
    }
}
