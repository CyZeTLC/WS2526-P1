package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;

public class JumpBoostBlock extends Block {
    public JumpBoostBlock(Location location) {
        super(location);
        this.setMaterial(Material.LAVA);
        this.setCollideAble(false);
    }

    @Override
    public void onCollide(Player player) {
        System.out.println("collide");
    }

    @Override
    public void update() {
        //Animation der Lava
    }
}
