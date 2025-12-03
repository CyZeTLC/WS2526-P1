package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class LavaBlock extends AnimatedBlock {
    private final List<ImageView> tiles = new ArrayList<>();
    private Image lastFrame;

    public LavaBlock(Location location) {
        super(location, new String[] {
                "/assets/lavaset/lava_1.png",
                "/assets/lavaset/lava_2.png",
                "/assets/lavaset/lava_3.png"
        });
        this.setMaterial(Material.LAVA);
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
    }

    @Override
    public void onCollide(Player player) {
        // Lava darf im GodMode keinen Schaden anrichten.
        if (player.isGodModeEnabled()) {
            return;
        }
        float newHealth = Math.max(0, player.getHealth() - 0.25f); // 25% Schaden pro Tick
        player.setHealth(newHealth);
        if (newHealth <= 0 && Game.getInstance() != null) {
            Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
        }
    }
}
