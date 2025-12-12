package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.block.PerkBlock;
import de.cyzetlc.hsbi.game.world.Location;

/**
 * The {@code FolderBlock} represents a collectible item within the game, symbolizing
 * a file that the player needs to acquire.
 * <p>
 * This block only becomes collectible and triggers an effect if the player is in the correct state
 * to collect files (checked via {@code Game.thePlayer.isCanCollectFiles()}).
 *
 * @see PerkBlock
 * @see Material#FOLDER_ITEM
 *
 * @author Tom Coombs
 */
public class FolderBlock extends PerkBlock {

    /**
     * Constructs a new {@code FolderBlock} at the specified location.
     * <p>
     * Initializes the block with the {@code FOLDER_ITEM} material and sets it as non-collidable,
     * meaning the player can overlap it to trigger the collection event.
     *
     * @param location The world location where the block should be placed.
     */
    public FolderBlock(Location location) {
        super(location);
        this.setMaterial(Material.FOLDER_ITEM);
        this.setCollideAble(false);
    }

    /**
     * Handles the collection logic when a player entity overlaps with the {@code FolderBlock}.
     * <p>
     * The block is only collected (and thus deactivated) if the player's {@code canCollectFiles}
     * flag is set to true. If collected, a click sound is played.
     *
     * @param player The {@code Player} instance that collided with the block.
     */
    @Override
    public void onCollide(Player player) {
        if (Game.thePlayer.isCanCollectFiles()) {
            this.setActive(false);
            SoundManager.play(Sound.CLICK);
        }
    }
}
