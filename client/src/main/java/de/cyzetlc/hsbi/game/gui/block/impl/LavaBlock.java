package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class LavaBlock extends AnimatedBlock {
    public LavaBlock(Location location) {
        super(location, new String[] {
                "/assets/lavaset/1Lava32x64.png",
                "/assets/lavaset/2Lava32x64.png",
                "/assets/lavaset/3Lava32x64.png"
        });
        this.setMaterial(Material.LAVA);
    }

    @Override
    public void onCollide(Player player) {
        float newHealth = Math.max(0, player.getHealth() - 0.25f); // 25% Schaden pro Tick
        player.setHealth(newHealth);
        if (newHealth <= 0 && Game.getInstance() != null) {
            Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
        }
    }
}
