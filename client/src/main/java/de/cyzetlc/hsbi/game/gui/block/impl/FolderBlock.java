package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.block.PerkBlock;
import de.cyzetlc.hsbi.game.world.Location;

public class FolderBlock extends PerkBlock {
    public FolderBlock(Location location) {
        super(location);
        this.setMaterial(Material.FOLDER_ITEM);
        this.setCollideAble(false);
    }

    @Override
    public void onCollide(Player player) {
        if (Game.thePlayer.isCanCollectFiles()) {
            this.setActive(false);
            SoundManager.play(Sound.CLICK);
        }
    }
}
