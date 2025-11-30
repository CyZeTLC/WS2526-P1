package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.layout.Pane;

public class LaserBlock extends Block {
    private final double speed;
    private final int direction; // -1 = left, 1 = right
    private double lifeSeconds = 0;
    private long lastUpdateNanos = 0L;

    public LaserBlock(Location location, int direction, double speed) {
        super(location);
        this.setMaterial(Material.ROBOT_LASER);
        this.setCollideAble(false);
        this.speed = speed;
        this.direction = direction >= 0 ? 1 : -1;
        this.setWidth(12);
        this.setHeight(12);
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.sprite.setPreserveRatio(true);
        this.sprite.setFitWidth(12);
        this.sprite.setFitHeight(12);
    }

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
