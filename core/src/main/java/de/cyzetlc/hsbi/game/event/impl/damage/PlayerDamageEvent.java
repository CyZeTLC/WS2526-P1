package de.cyzetlc.hsbi.game.event.impl.damage;

import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.event.Event;
import lombok.Getter;

@Getter
public class PlayerDamageEvent extends Event {
    private Player player;

    private double damage;

    private DamageCause cause;

    private double newHealth;

    public PlayerDamageEvent(Player player, double damage, DamageCause cause) {
        this.player = player;
        this.damage = damage;
        this.cause = cause;
        this.newHealth = player.getHealth() - damage;
    }
}

