package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;
import lombok.Getter;

public class RobotEnemyBlock extends Block {
    protected final double minX;
    protected final double maxX;
    protected double baseY;
    protected final double speed;
    @Getter
    protected boolean dead = false;
    private long lastUpdateNanos = 0L;
    protected double fireTimer = 0;
    protected final double fireCooldown = 1.2;
    private double lastHitTime = -1;

    public RobotEnemyBlock(Location location, double patrolWidth, double speed) {
        super(location);
        this.setMaterial(Material.ROBOT_ENEMY);
        this.setCollideAble(true);
        this.setWidth(48);
        this.setHeight(96);
        this.minX = location.getX();
        this.maxX = location.getX() + Math.max(0, patrolWidth);
        this.baseY = location.getY();
        this.speed = speed;
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.setWidth((float) this.sprite.getBoundsInLocal().getWidth());
        this.setHeight((float) this.sprite.getBoundsInLocal().getHeight());
        this.getLocation().setY(this.baseY);
        if (this.sprite.getParent() != null) {
            this.sprite.toFront();
        }
    }

    @Override
    public void onCollide(de.cyzetlc.hsbi.game.entity.Player player) {
        // handled in GameScreen to avoid double processing
    }

    public void kill() {
        this.dead = true;
        this.setActive(false);
        this.setCollideAble(false);
    }

    @Override
    public void update() {
        super.update();
        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
            return;
        }
        double delta = (now - lastUpdateNanos) / 1_000_000_000.0;
        lastUpdateNanos = now;

        if (dead) return;

        double targetX = Game.thePlayer != null ? Game.thePlayer.getLocation().getX() : this.getLocation().getX();
        double clampedTarget = Math.max(minX, Math.min(maxX - this.getWidth(), targetX));

        double currentX = this.getLocation().getX();
        double dx = clampedTarget - currentX;
        double step = Math.signum(dx) * speed * delta;
        if (Math.abs(step) > Math.abs(dx)) {
            step = dx;
        }
        double nextX = currentX + step;

        this.getLocation().setX(nextX);
        this.getLocation().setY(baseY);

        this.updateFacingToPlayer();

        fireTimer += delta;
    }

    public LaserBlock tryFire(EntityPlayer player) {
        if (dead || player == null) return null;

        // Spieler muss deutlich oberhalb sein (Sprung ueber den Boss)
        //boolean playerAbove = player.getLocation().getY() + player.getHeight() < this.getLocation().getY();
        boolean closeHorizontally = Math.abs(player.getLocation().getX() - this.getLocation().getX()) < 440;
        if (!closeHorizontally) {
            return null;
        }
        if (fireTimer < fireCooldown) {
            return null;
        }
        fireTimer = 0;

        int dir = player.getLocation().getX() >= this.getLocation().getX() ? 1 : -1;
        double eyeY = this.getLocation().getY() + this.getHeight() * 0.35;
        double spawnX = dir == 1 ? this.getLocation().getX() + this.getWidth() - 4 : this.getLocation().getX() - 8;

        return new LaserBlock(new Location(spawnX, eyeY), dir, 320);
    }

    public void hitPlayer(de.cyzetlc.hsbi.game.entity.Player player) {
        if (dead) return;
        // Treffer ignorieren, wenn der Spieler unverwundbar ist.
        if (player.isGodModeEnabled()) {
            return;
        }
        double now = System.nanoTime() / 1e9;
        if (this.lastHitTime < 0 || now - this.lastHitTime > 0.5) {
            player.setHealth(player.getHealth() - 1f);
            this.lastHitTime = now;
        }
    }

    private void updateFacingToPlayer() {
        if (this.sprite == null) {
            return;
        }
        double playerX = Game.thePlayer != null ? Game.thePlayer.getLocation().getX() : this.getLocation().getX();
        boolean faceRight = playerX >= this.getLocation().getX();
        this.sprite.setScaleX(faceRight ? 1 : -1);
        this.sprite.toFront();
    }
}