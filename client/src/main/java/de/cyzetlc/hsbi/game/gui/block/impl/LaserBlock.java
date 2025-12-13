package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

/**
 * The {@code LaserBlock} represents a projectile fired by an enemy, designed to move
 * horizontally and inflict damage upon collision with the player.
 * <p>
 * This block has a limited lifetime (4.0 seconds) and automatically disappears upon impact or expiration.
 * The movement is calculated based on the time elapsed between frames (`delta`).
 *
 * @see Block
 * @see Material#ROBOT_LASER
 *
 * @author Leonardo Parrino
 */
public class LaserBlock extends Block {
    /**
     * The constant speed at which the laser projectile travels (units per second).
     */
    private final double speed;

    /**
     * The horizontal direction of movement: 1 for right, -1 for left.
     */
    private final int direction;

    /**
     * Tracks the total time (in seconds) the laser has been active since creation.
     */
    private double lifeSeconds = 0;

    /**
     * The timestamp of the last update cycle in nanoseconds, used to calculate delta time.
     */
    private long lastUpdateNanos = 0L;

    /**
     * Constructs a new {@code LaserBlock} instance, initializing its movement properties and appearance.
     *
     * @param location The initial world location of the laser.
     * @param direction The initial direction of movement (any non-negative value is right, negative is left).
     * @param speed The constant movement speed of the laser.
     */
    public LaserBlock(Location location, int direction, double speed) {
        super(location);
        this.setMaterial(Material.ROBOT_LASER);
        this.setCollideAble(false);
        this.speed = speed;
        this.direction = direction >= 0 ? 1 : -1;
        this.setWidth(12);
        this.setHeight(12);
    }

    /**
     * Overrides the default drawing method to adjust the visual size of the laser sprite
     * while keeping the collision box size constant.
     *
     * @param pane The {@code Pane} where the laser is drawn.
     */
    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.sprite.setPreserveRatio(true);
        this.sprite.setFitWidth(24);
        this.sprite.setFitHeight(24);
    }

    /**
     * Handles collision logic when the laser hits a player.
     * <p>
     * If the player is in God Mode, the laser is simply consumed and disappears.
     * Otherwise, the player takes 1 point of damage, and the laser is deactivated.
     *
     * @param player The {@code Player} entity that collided with the laser.
     */
    @Override
    public void onCollide(de.cyzetlc.hsbi.game.entity.Player player) {
        if (!this.isActive()) return;
        // GodMode frisst den Treffer, aber der Laser verschwindet trotzdem.
        if (player.isGodModeEnabled()) {
            this.setActive(false);
            return;
        }
        player.setHealth(player.getHealth() - 1f);
        this.setActive(false);
    }

    /**
     * Updates the laser's position and lifetime based on the time elapsed since the last frame.
     * <p>
     * Calculates the delta time (`delta`), checks if the laser's lifetime exceeds 4.0 seconds,
     * and updates the horizontal position based on speed and direction.
     */
    @Override
    public void update() {
        long now = System.nanoTime();
        if (lastUpdateNanos == 0L) {
            lastUpdateNanos = now;
            return;
        }
        double delta = (now - lastUpdateNanos) / 1_000_000_000.0;
        lastUpdateNanos = now;

        lifeSeconds += delta;
        if (lifeSeconds > 4.0) {
            this.setActive(false);
            return;
        }

        double dx = direction * speed * delta;
        this.getLocation().setX(this.getLocation().getX() + dx);

        super.update();
    }
}
