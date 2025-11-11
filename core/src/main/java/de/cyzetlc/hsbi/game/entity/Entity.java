package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.world.Location;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public abstract class Entity {
    private final UUID uuid;

    private Location location;
    private String displayName;

    private float health;

    private float width;
    private float height;

    public Entity() {
        this.uuid = UUID.randomUUID();
        this.health = 0.0F;
    }

    public abstract void update();
}
