package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.gui.screens.LevelFinishedScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.level.impl.SecondLevel;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class FinishBlock extends AnimatedBlock {
    public FinishBlock(Location location) {
        super(location, new String[] {
                "/assets/tileset/finish/finish_first.png",
                "/assets/tileset/finish/finish_second.png"
        });
        this.setMaterial(Material.FINISH_FLAG);
        this.setCollideAble(false);
        this.setWidth(45);
        this.setHeight(90);
    }

    @Override
    public void onCollide(Player player) {
        Game.getInstance().getScreenManager().showScreen(new LevelFinishedScreen(Game.getInstance().getScreenManager()));
    }
}
