package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.world.Location;

public abstract class PerkBlock extends Block {
    private final double FRAME_DURATION_SECONDS = 0.6;

    private double frameTimer = 0;
    private long lastFrameTimeNanos = 0L;
    private boolean upDown = false;

    public PerkBlock(Location location) {
        super(location);
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
        if (this.frameTimer >= FRAME_DURATION_SECONDS) {
            this.frameTimer -= FRAME_DURATION_SECONDS;
            if (this.upDown) {
                this.getLocation().setY(this.getLocation().getY() + 5);
                this.upDown = false;
            } else {
                this.getLocation().setY(this.getLocation().getY() - 5);
                this.upDown = true;
            }
        }
    }
}
