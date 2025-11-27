package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.block.PerkBlock;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

public class FlipperBlock extends PerkBlock {
    private boolean collected = false;
    private final double baseY;
    private double frameTimer = 0;
    private long lastFrameTimeNanos = 0L;
    private boolean upDown = false;

    public FlipperBlock(Location location) {
        super(location);
        this.setMaterial(Material.FLIPPER);
        this.setCollideAble(false);
        this.setWidth(0);
        this.setHeight(0);
        this.baseY = location.getY();
    }

    @Override
    public void onCollide(Player player) {
        if (this.collected) {
            return;
        }
        this.collected = true;
        SoundManager.play(Sound.CLICK);
        this.setActive(false);
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.sprite.setPreserveRatio(true);

        double targetWidth = 140;
        this.sprite.setFitWidth(targetWidth);
        this.sprite.setFitHeight(targetWidth); // height will be reduced by preserveRatio

        double camX = 0;
        double camY = 0;
        if (Game.getInstance() != null && Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen) {
            GameScreen gs = (GameScreen) Game.getInstance().getScreenManager().getCurrentScreen();
            camX = gs.getCameraX();
            camY = gs.getCameraY();
        }

        this.sprite.setX(this.getLocation().getX() - camX);
        this.sprite.setY(this.getLocation().getY() - camY);

        this.setWidth((float) this.sprite.getBoundsInLocal().getWidth());
        this.setHeight((float) this.sprite.getBoundsInLocal().getHeight());
    }

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
        if (this.frameTimer >= 0.6) {
            this.frameTimer -= 0.6;
            if (this.upDown) {
                this.getLocation().setY(Math.max(baseY - 10, this.getLocation().getY() - 5));
                this.upDown = false;
            } else {
                double nextY = this.getLocation().getY() + 5;
                this.getLocation().setY(Math.min(baseY, nextY));
                this.upDown = true;
            }
        }
    }
}
