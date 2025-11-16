package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.event.impl.damage.DamageCause;
import de.cyzetlc.hsbi.game.event.impl.damage.PlayerDamageEvent;

import java.util.Random;

public class Player extends Entity {
    public Player() {
        super();
        this.setDisplayName("Player" + new Random().nextInt(999));
        this.setHealth(5.0F);
    }

    @Override
    public void setHealth(float health) {
        if (health < this.getHealth()) {
            new PlayerDamageEvent(this, this.getHealth()-health, DamageCause.ITEM).call();
        }
        super.setHealth(health);
    }

    @Override
    public void update() {

    }
}
