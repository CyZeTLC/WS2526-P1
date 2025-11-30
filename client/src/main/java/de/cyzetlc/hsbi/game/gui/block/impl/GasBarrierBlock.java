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
    /**
     * Flag ob die Schranke noch tötet / blockiert.
     */
    private boolean active = true;
    /**
     * Fade-out Zustand nachdem der Spieler per E interagiert hat.
     */
    private boolean fadingOut = false;
    /**
     * Transparenz für manuelles Ausblenden, falls kein FadeTransition läuft.
     */
    private double alpha = 1.0;

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
        this.sprite.setOpacity(alpha);
    }

    @Override
    public void onCollide(Player player) {
        // Solange aktiv: sofort Schaden/Tod auslösen
        if (!active) return;
        player.setHealth(0f);
    }

    public void deactivate() {
        if (fadingOut || !active) return;
        fadingOut = true;
        // Kollision sofort deaktivieren, sobald der Spieler korrekt interagiert hat
        this.setCollideAble(false);
        SoundManager.play(Sound.CLICK);
        FadeTransition ft = new FadeTransition(Duration.seconds(0.8), this.sprite);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            this.active = false;
            this.setActive(false);
            this.alpha = 0;
        });
        ft.play();
    }

    @Override
    public void update() {
        super.update();
        // Falls aus irgendeinem Grund das FadeTransition nicht läuft: manuelles Ausblenden
        if (fadingOut && active) {
            alpha = Math.max(0, alpha - 0.05);
            this.sprite.setOpacity(alpha);
            if (alpha <= 0.0) {
                active = false;
                this.setActive(false);
            }
        }
    }
}
