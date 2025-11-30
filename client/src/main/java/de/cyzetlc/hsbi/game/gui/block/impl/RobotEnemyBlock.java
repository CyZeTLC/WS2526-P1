package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

public class RobotEnemyBlock extends Block {
    private final double minX;
    private final double maxX;
    private final double baseY;
    private final double speed;
    private boolean dead = false;
    private long lastUpdateNanos = 0L;
    private double fireTimer = 0;
    private final double fireCooldown = 2.2;
    private double lastHitTime = -1;

    public RobotEnemyBlock(Location location, double patrolWidth, double speed) {
        super(location);
        this.setMaterial(Material.ROBOT_ENEMY);
        this.setCollideAble(true);
        this.setWidth(0);
        this.setHeight(0);
        this.minX = location.getX();
        this.maxX = location.getX() + Math.max(0, patrolWidth);
        this.baseY = location.getY();
        this.speed = speed;
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.sprite.setPreserveRatio(true);
        this.sprite.setFitWidth(96);
        this.sprite.setFitHeight(96);
        this.setWidth((float) this.sprite.getBoundsInLocal().getWidth());
        this.setHeight((float) this.sprite.getBoundsInLocal().getHeight());
        if (this.sprite.getParent() != null) {
            this.sprite.toFront();
        }
    }

    @Override
    public void onCollide(de.cyzetlc.hsbi.game.entity.Player player) {
        // handled in GameScreen to avoid double processing
    }

    public boolean isDead() {
        return dead;
    }

    public void kill() {
        this.dead = true;
        this.setActive(false);
        this.setCollideAble(false);
    }

    @Override
    public void update() {
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
        boolean playerIsAbove = player.getLocation().getY() + player.getHeight() < this.getLocation().getY() + this.getHeight() * 0.5;
        boolean closeHorizontally = Math.abs(player.getLocation().getX() - this.getLocation().getX()) < 220;
        if (fireTimer < fireCooldown && !(playerIsAbove && closeHorizontally)) return null;
        fireTimer = 0;

        double playerX = player.getLocation().getX();
        int dir = playerX >= this.getLocation().getX() ? 1 : -1;
        double eyeY = this.getLocation().getY() + this.getHeight() * 0.35;
        double spawnX = dir == 1 ? this.getLocation().getX() + this.getWidth() - 4 : this.getLocation().getX() - 8;

        LaserBlock laser = new LaserBlock(new Location(spawnX, eyeY), dir, 280);
        return laser;
    }

    public void hitPlayer(de.cyzetlc.hsbi.game.entity.Player player) {
        if (dead) return;
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
