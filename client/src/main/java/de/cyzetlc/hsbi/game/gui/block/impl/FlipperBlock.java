package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.block.PerkBlock;
import de.cyzetlc.hsbi.game.world.Location;

public class FlipperBlock extends PerkBlock {
    private boolean collected = false;

    public FlipperBlock(Location location) {
        super(location);
        this.setMaterial(Material.FLIPPER);
        this.setCollideAble(false);
        this.setWidth(96);
        this.setHeight(48);
    }

    @Override
    public void onCollide(Player player) {
        if (this.collected) {
            return;
        }
        this.collected = true;
        SoundManager.play(Sound.CLICK);
        this.setActive(false);
    }
}
