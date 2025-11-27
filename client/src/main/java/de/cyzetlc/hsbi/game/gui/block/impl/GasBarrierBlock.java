package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.animation.FadeTransition;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class GasBarrierBlock extends Block {
    private boolean deactivating = false;

    public GasBarrierBlock(Location location, double width, double height) {
        super(location);
        this.setMaterial(Material.GAS_BARRIER);
        this.setCollideAble(true);
        this.setWidth(width);
        this.setHeight(height);
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        this.sprite.setFitWidth(this.getWidth());
        this.sprite.setFitHeight(this.getHeight());
    }

    @Override
    public void onCollide(Player player) {
        if (deactivating) return;
        player.setHealth(player.getHealth() - 1f);
    }

    public void deactivate() {
        if (deactivating) return;
        deactivating = true;
        SoundManager.play(Sound.CLICK);
        FadeTransition ft = new FadeTransition(Duration.seconds(0.8), this.sprite);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> this.setActive(false));
        ft.play();
        this.setCollideAble(false);
    }
}
