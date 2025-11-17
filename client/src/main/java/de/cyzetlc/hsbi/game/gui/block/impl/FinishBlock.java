package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;

public class FinishBlock extends AnimatedBlock {
    public FinishBlock(Location location) {
        super(location, new String[] {
                "/assets/tileset/finish/finish_first.png",
                "/assets/tileset/finish/finish_second.png"
        });
        this.setMaterial(Material.SERVER);
        this.setCollideAble(false);
        this.setWidth(45);
        this.setHeight(90);
    }

    @Override
    public void onCollide(Player player) {
        player.setHealth(0.0F);
    }
}
