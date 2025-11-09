package de.cyzetlc.hsbi.game.entity;

import java.util.Random;

public class Player extends Entity {
    public Player() {
        this.setDisplayName("Player" + new Random().nextInt(999));
    }

    @Override
    public void update() {

    }
}
