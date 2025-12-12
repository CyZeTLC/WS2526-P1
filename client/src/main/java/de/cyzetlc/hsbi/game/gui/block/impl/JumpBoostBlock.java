package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.block.PerkBlock;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * The {@code JumpBoostBlock} represents a temporary power-up that, when collected by the player,
 * significantly increases the player's jump strength for a limited duration.
 * <p>
 * This block is a single-use collectible that triggers an effect, then deactivates and
 * schedules a reset of the player's jump power after a set timeout.
 *
 * @see PerkBlock
 * @see Material#JUMP_PERK
 *
 * @author Tom Coombs
 */
public class JumpBoostBlock extends PerkBlock {
    /**
     * Flag indicating whether this boost block has already been triggered by the player.
     * Prevents the effect from stacking if the player remains in collision.
     */
    private boolean isTriggered = false;

    /**
     * Constructs a new {@code JumpBoostBlock} at the specified location.
     * <p>
     * Initializes the block with the {@code JUMP_PERK} material and sets it as non-collidable,
     * meaning the player can overlap it to trigger the perk.
     *
     * @param location The world location where the block should be placed.
     */
    public JumpBoostBlock(Location location) {
        super(location);
        this.setMaterial(Material.JUMP_PERK);
        this.setCollideAble(false);
    }

    /**
     * Handles the logic executed when a player entity collides (overlaps) with the {@code JumpBoostBlock}.
     * <p>
     * If not already triggered:
     * <ul>
     * <li>Increases the player jump power (Game.jumpPower) by 25%.</li>
     * <li>Plays a boosted sound effect while ducking the background music (if the tutorial is not finished).</li>
     * <li>Deactivates the block and schedules a {@code PauseTransition} to reset the jump power after 10 seconds.</li>
     * </ul>
     *
     * @param player The {@code Player} instance that collided with the block.
     */
    @Override
    public void onCollide(Player player) {
        if (!this.isTriggered) {
            Game.jumpPower *= 1.25; // 25% hoehere Sprungkraft
            this.isTriggered = true;
            this.setActive(false); // block verschwindet

            if (!Game.getInstance().getConfig().getObject().getBoolean("tutorialFinished")) {
                SoundManager.playWithDuck(Sound.JUMP_BOOST, 1.0, 0.06);
            }

            PauseTransition delay = new PauseTransition(Duration.seconds(10));
            delay.setOnFinished(event -> {
                this.isTriggered = false;
                Game.jumpPower = 800;
            });
            delay.play();
        }
    }
}
