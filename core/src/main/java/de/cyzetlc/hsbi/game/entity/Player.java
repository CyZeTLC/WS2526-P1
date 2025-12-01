package de.cyzetlc.hsbi.game.entity;

import de.cyzetlc.hsbi.game.event.impl.damage.DamageCause;
import de.cyzetlc.hsbi.game.event.impl.damage.PlayerDamageEvent;

import java.util.Random;

public class Player extends Entity {
    private boolean flipperCollected = false;
    // Debug/Utility Flags
    private boolean noClip = false;   // true = Kollisionen ignorieren / frei bewegen
    private boolean godMode = false;  // true = kein Schaden

    public Player() {
        super();
        this.setDisplayName("Player" + new Random().nextInt(999));
        this.setMaxHealth(3.0F);
        this.setHealth(3.0F);
    }

    @Override
    public void setHealth(float health) {
        // Im GodMode ignorieren wir jede Reduktion der Lebenspunkte, damit der Spieler nicht sterben kann.
        if (this.isGodModeEnabled() && health < this.getHealth()) {
            return;
        }
        if (health < this.getHealth()) {
            new PlayerDamageEvent(this, this.getHealth()-health, DamageCause.ITEM).call();
        }
        super.setHealth(health);
    }

    @Override
    public void update() {

    }

    public boolean hasFlipper() {
        return flipperCollected;
    }

    public void setFlipperCollected(boolean flipperCollected) {
        this.flipperCollected = flipperCollected;
    }

    public boolean isNoClipEnabled() {
        return noClip;
    }

    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
    }

    public boolean isGodModeEnabled() {
        return godMode;
    }

    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
    }
}
