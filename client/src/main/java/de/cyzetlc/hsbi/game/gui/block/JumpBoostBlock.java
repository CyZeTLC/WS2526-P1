package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class JumpBoostBlock extends Block {
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

            SoundManager.play(Sound.CLICK);

            PauseTransition delay = new PauseTransition(Duration.seconds(10));
            delay.setOnFinished(event -> {
                this.isTriggered = false;
                Game.jumpPower = 800;
                System.out.println("reset");
            });
            delay.play();
        }
    }

    @Override
    public void update() {
        super.update();
    }
}
