package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.world.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public abstract class Entity {
    private UUID uuid;

    private Location location;
    private String displayName;

    private float health;
    private float maxHealth;

    private float width;
    private float height;

    public Entity() {
        this.uuid = UUID.randomUUID();
        this.maxHealth = 1.0F; // 100% Leben als 1.0
        this.health = this.maxHealth;
    }

    public void setHealth(float health) {
        this.health = Math.max(0F, Math.min(health, this.maxHealth));
    }

    public abstract void update();
}
