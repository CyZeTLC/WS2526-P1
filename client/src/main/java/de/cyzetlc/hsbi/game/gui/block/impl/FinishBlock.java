package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
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
        Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            Game.getInstance().setCurrentLevel(new SecondLevel());
            Game.getInstance().getScreenManager().showScreen(new GameScreen(Game.getInstance().getScreenManager()));
        });
        delay.play();
    }
}
