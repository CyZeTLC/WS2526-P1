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

public class JumpBoostBlock extends PerkBlock {
    private boolean isTriggered = false;

    public JumpBoostBlock(Location location) {
        super(location);
        this.setMaterial(Material.JUMP_PERK);
        this.setCollideAble(false);
    }

    @Override
    public void onCollide(Player player) {
        if (!this.isTriggered) {
            Game.jumpPower *= 1.25; // 25% hoehere Sprungkraft
            this.isTriggered = true;
            this.setActive(false); // block verschwindet

            SoundManager.play(Sound.JUMP_BOOST, 0.35);

            PauseTransition delay = new PauseTransition(Duration.seconds(10));
            delay.setOnFinished(event -> {
                this.isTriggered = false;
                Game.jumpPower = 800;
            });
            delay.play();
        }
    }
}
