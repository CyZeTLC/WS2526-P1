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

/**
 * The {@code LavaBlock} represents a deadly animated environmental hazard.
 * When the player collides (overlaps) with this block, the player takes recurring damage.
 * <p>
 * This block extends {@code AnimatedBlock} to display a flowing lava texture.
 * Collision logic is handled specifically to bypass damage if God Mode is active.
 *
 * @see AnimatedBlock
 * @see Material#LAVA
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class LavaBlock extends AnimatedBlock {
    /**
     * Constructs a new {@code LavaBlock} at the specified location.
     * <p>
     * Initializes the block with the animation frames for lava and sets its material type.
     *
     * @param location The world location (top-left corner) where the block should be placed.
     */
    public LavaBlock(Location location) {
        super(location, new String[] {
                "/assets/lavaset/lava_1.png",
                "/assets/lavaset/lava_2.png",
                "/assets/lavaset/lava_3.png"
        });
        this.setMaterial(Material.LAVA);
    }

    /**
     * Handles the collision logic when a player entity overlaps with this block,
     * resulting in instantaneous death if God Mode is not active.
     * <p>
     * If the player is in God Mode, the collision is ignored. Otherwise, the player's
     * health is immediately set to zero, triggering the death sequence (handled elsewhere).
     *
     * @param player The {@code Player} instance that collided with the block.
     */
    @Override
    public void onCollide(Player player) {
        // Lava darf im GodMode keinen Schaden anrichten.
        if (player.isGodModeEnabled()) {
            return;
        }
        player.setHealth(0.0F);
    }
}
