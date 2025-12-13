package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.utils.ui.ImageAssets;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;
import lombok.Getter;

/**
 * The {@code RobotEnemyBlock} represents a persistent, patrolling enemy entity (a robot)
 * that moves horizontally within a defined range and periodically fires projectiles at the player.
 * <p>
 * This block implements basic AI logic to track the player's horizontal position within its patrol area
 * and manage a firing cooldown.
 *
 * @see Block
 * @see LaserBlock
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class RobotEnemyBlock extends Block {
    /**
     * The minimum X-coordinate of the enemy's horizontal patrol range.
     */
    protected final double minX;

    /**
     * The maximum X-coordinate of the enemy's horizontal patrol range.
     */
    protected final double maxX;

    /**
     * The fixed Y-coordinate of the enemy, ensuring it remains grounded on its platform.
     */
    protected double baseY;

    /**
     * The constant speed at which the robot moves horizontally while patrolling or chasing (units per second).
     */
    protected final double speed;

    /**
     * Flag indicating whether the enemy has been defeated. A dead enemy is inactive and non-collidable.
     */
    @Getter
    protected boolean dead = false;

    /**
     * The timestamp of the last update cycle in nanoseconds, used to calculate delta time for movement and timers.
     */
    private long lastUpdateNanos = 0L;

    /**
     * Accumulator for the time elapsed since the last projectile was fired.
     */
    protected double fireTimer = 0;

    /**
     * The minimum required time (in seconds) between firing projectiles.
     */
    protected final double fireCooldown = 1.2;

    /**
     * Timestamp (in seconds) of the last time the player was successfully hit by this enemy.
     * Used to implement hit invulnerability/cooldown for the player.
     */
    private double lastHitTime = -1;

    /**
     * Constructs a new {@code RobotEnemyBlock} instance.
     *
     * @param location The initial world location (top-left corner).
     * @param patrolWidth The total width of the area the robot should patrol (measured from the initial X).
     * @param speed The horizontal movement speed.
     */
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

    /**
     * Draws the robot enemy, ensuring its visual bounds match its collision bounds and
     * maintaining its fixed vertical position. Also brings the sprite to the front.
     *
     * @param pane The {@code Pane} where the block is drawn.
     */
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

    /**
     * Placeholder for the collision logic. Actual collision processing (checking if the
     * player jumps on the enemy) is often handled centrally in the {@code GameScreen}
     * or level update loop to avoid double processing.
     *
     * @param player The {@code Player} entity that collided with the enemy.
     */
    @Override
    public void onCollide(de.cyzetlc.hsbi.game.entity.Player player) {
        // handled in GameScreen to avoid double processing
    }

    /**
     * Marks the enemy as dead, deactivates the block, and disables collisions.
     */
    public void kill() {
        this.dead = true;
        this.setActive(false);
        this.setCollideAble(false);
    }

    /**
     * Updates the robot's movement and internal timers.
     * <p>
     * Calculates delta time, determines a target X position (clamped to the patrol range,
     * chasing the player), updates the robot's position based on speed, and updates the
     * facing direction.
     */
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

    /**
     * Attempts to create and return a new {@code LaserBlock} projectile if the enemy is not dead,
     * the player is within a close horizontal range, and the firing cooldown has expired.
     * <p>
     * If a laser is created, the {@code fireTimer} is reset. The laser is spawned from the
     * approximate "eye" level of the robot.
     *
     * @param player The target player entity.
     * @return A new {@code LaserBlock} if ready to fire, or {@code null} otherwise.
     */
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