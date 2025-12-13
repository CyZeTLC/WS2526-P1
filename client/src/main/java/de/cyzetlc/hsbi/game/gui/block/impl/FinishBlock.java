package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.gui.screens.LevelFinishedScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.level.impl.SecondLevel;
import de.cyzetlc.hsbi.game.level.impl.TutorialLevel;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * The {@code FinishBlock} represents the end goal or exit point of a game level.
 * When the player collides with this block, the current level is considered finished,
 * triggering the display of the {@code LevelFinishedScreen} and handling the transition
 * to the next level or resetting the game state if all levels are complete.
 * <p>
 * This block is visually animated and non-collidable (only triggers an effect upon overlap).
 *
 * @see AnimatedBlock
 * @see LevelFinishedScreen
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class FinishBlock extends AnimatedBlock {

    /**
     * Constructs a new {@code FinishBlock} at the specified location.
     * <p>
     * Initializes the block with a set of animation frames, sets its material,
     * disables collision, and defines its size.
     *
     * @param location The world location where the block should be placed.
     */
    public FinishBlock(Location location) {
        super(location, new String[] {
                "/assets/USB-Stick/PortalTutorial/Portal1-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal2-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal3-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal4-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal5-removebg-preview.png"
        });
        this.setMaterial(Material.FINISH_FLAG);
        this.setCollideAble(false);
        this.setWidth(90);
        this.setHeight(90);
    }

    /**
     * Handles the logic executed when a player entity collides (overlaps) with the {@code FinishBlock}.
     * <p>
     * This method performs the following critical actions:
     * <ul>
     * <li>Calls {@code onFinish()} on the current level.</li>
     * <li>Shows the {@code LevelFinishedScreen}.</li>
     * <li>Determines the next level based on {@code getCurrentLevel().getNextLevel()}.</li>
     * <li>Saves the name of the next level to the configuration or resets to "Tutorial" if the game is completed.</li>
     * </ul>
     *
     * @param player The {@code Player} instance that collided with the block.
     */
    @Override
    public void onCollide(Player player) {
        Game.getLogger().info(Game.getInstance().getCurrentLevel().getName() + " finished!");
        Game.getInstance().getCurrentLevel().onFinish();
        Game.getInstance().getScreenManager().showScreen(new LevelFinishedScreen(Game.getInstance().getScreenManager()));

        if (Game.getInstance().getCurrentLevel().getNextLevel() != null) {
            Game.getLogger().info("Loading & saving next level..");
            Game.getInstance().getConfig().getObject().put("currentLevel", Game.getInstance().getCurrentLevel().getNextLevel().getName());
            Game.getInstance().getConfig().save();
        } else {
            Game.getLogger().info("Game successfully finished (no more levels left)!");
            Game.getInstance().setCurrentLevel(new TutorialLevel());
            Game.getInstance().getConfig().getObject().put("currentLevel", "Tutorial");
            Game.getInstance().getConfig().save();
        }
    }
}
