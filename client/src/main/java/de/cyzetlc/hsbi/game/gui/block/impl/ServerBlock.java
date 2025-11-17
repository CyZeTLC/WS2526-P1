package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;

public class ServerBlock extends AnimatedBlock {
    public ServerBlock(Location location) {
        super(location, new String[] {
                "/assets/hud/PlatformServerMehreZustände/Zugeschnitten1.png",
                "/assets/hud/PlatformServerMehreZustände/Zugeschnitten1 - Lichtzustand1.png"
        });
        this.setMaterial(Material.SERVER);
        this.setWidth(64);
        this.setHeight(128);
    }

    @Override
    public void onCollide(Player player) {

    }
}
