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
 * Der {@code JumpBoostBlock} repräsentiert ein temporäres Power-up, das, wenn es vom Spieler eingesammelt wird,
 * die Sprungkraft des Spielers für eine begrenzte Dauer deutlich erhöht.
 * <p>
 * Dieser Block ist ein einmalig einsammelbares Objekt, das einen Effekt auslöst, sich dann deaktiviert und
 * eine Rücksetzung der Sprungkraft des Spielers nach einer festgelegten Zeitverzögerung plant.
 *
 *
 * @see PerkBlock
 * @see Material#JUMP_PERK
 *
 * @author Tom Coombs
 */
public class JumpBoostBlock extends PerkBlock {
    /**
     * Flag, das anzeigt, ob dieser Boost-Block bereits vom Spieler ausgelöst wurde.
     * Verhindert das Stapeln des Effekts, falls der Spieler in der Kollision verbleibt.
     */
    private boolean isTriggered = false;

    /**
     * Konstruiert einen neuen {@code JumpBoostBlock} an der angegebenen Position.
     * <p>
     * Initialisiert den Block mit dem Material {@code JUMP_PERK} und setzt ihn als nicht kollidierbar,
     * was bedeutet, dass der Spieler ihn überlappen kann, um den Perk auszulösen.
     *
     * @param location Die Weltposition, an der der Block platziert werden soll.
     */
    public JumpBoostBlock(Location location) {
        super(location);
        this.setMaterial(Material.JUMP_PERK);
        this.setCollideAble(false);
    }

    /**
     * Behandelt die Logik, die ausgeführt wird, wenn eine Spieler-Entität mit dem {@code JumpBoostBlock}
     * kollidiert (überlappt).
     * <p>
     * Falls noch nicht ausgelöst:
     * <ul>
     * <li>Erhöht die Sprungkraft des Spielers (Game.jumpPower) um 25%.</li>
     * <li>Spielt einen Boost-Soundeffekt ab und dämpft dabei die Hintergrundmusik (falls das Tutorial nicht beendet ist).</li>
     * <li>Deaktiviert den Block und plant eine {@code PauseTransition} zur Rücksetzung der Sprungkraft nach 10 Sekunden.</li>
     * </ul>
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
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