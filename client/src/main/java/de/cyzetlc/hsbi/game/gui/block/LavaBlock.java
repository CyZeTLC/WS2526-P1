package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.world.Location;

public class LavaBlock extends Block {
    public LavaBlock(Location location) {
        super(location);
        this.setMaterial(Material.DIRT);
    }

    @Override
    public void onCollide(Player player) {
        // Spieler töten
        player.setHealth(0.0F);
    }

    @Override
    public void update() {
        //Animation der Lava
    }
}
