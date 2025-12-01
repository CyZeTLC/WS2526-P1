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

public class FinishBlock extends AnimatedBlock {
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
