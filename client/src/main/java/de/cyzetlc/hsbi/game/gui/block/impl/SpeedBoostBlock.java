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
 * Der {@code SpeedBoostBlock} repräsentiert ein temporäres Power-up, das, wenn es vom Spieler eingesammelt wird,
 * die horizontale Bewegungsgeschwindigkeit des Spielers für eine begrenzte Dauer deutlich erhöht.
 *
 * <p>
 * Dieser Block ist ein einmalig einsammelbares Objekt, das einen Effekt auslöst, sich dann deaktiviert und
 * eine Rücksetzung der Bewegungsgeschwindigkeit des Spielers nach einer festgelegten Zeitverzögerung plant.
 *
 * @see PerkBlock
 * @see Material#SPEED_PERK
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class SpeedBoostBlock extends PerkBlock {
    /**
     * Flag, das anzeigt, ob dieser Boost-Block bereits vom Spieler ausgelöst wurde.
     * Verhindert das Stapeln des Effekts, falls der Spieler in der Kollision verbleibt.
     */
    private boolean isTriggered = false;

    /**
     * Konstruiert einen neuen {@code SpeedBoostBlock} an der angegebenen Position.
     * <p>
     * Initialisiert den Block mit dem Material {@code SPEED_PERK} und setzt ihn als nicht kollidierbar,
     * was bedeutet, dass der Spieler ihn überlappen kann, um den Perk auszulösen.
     *
     * @param location Die Weltposition, an der der Block platziert werden soll.
     */
    public SpeedBoostBlock(Location location) {
        super(location);
        this.setMaterial(Material.SPEED_PERK);
        this.setCollideAble(false);
    }

    /**
     * Behandelt die Logik, die ausgeführt wird, wenn eine Spieler-Entität mit dem {@code SpeedBoostBlock}
     * kollidiert (überlappt).
     * <p>
     * Falls noch nicht ausgelöst:
     * <ul>
     * <li>Erhöht die Bewegungsgeschwindigkeit des Spielers (Game.moveSpeed) um 25%.</li>
     * <li>Spielt einen Boost-Soundeffekt ab und dämpft dabei die Hintergrundmusik.</li>
     * <li>Deaktiviert den Block und plant eine {@code PauseTransition} zur Rücksetzung der Geschwindigkeit nach 10 Sekunden.</li>
     * </ul>
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    @Override
    public void onCollide(Player player) {
        if (!this.isTriggered) {
            Game.moveSpeed *= 1.25; // 25% hoehere Geschwindigkeit
            this.isTriggered = true;
            this.setActive(false); // block verschwindet

            SoundManager.playWithDuck(Sound.SPEED_BUFF, 1.0, 0.06);

            PauseTransition delay = new PauseTransition(Duration.seconds(10));
            delay.setOnFinished(event -> {
                this.isTriggered = false;
                Game.moveSpeed = 450;
            });
            delay.play();
        }
    }
}