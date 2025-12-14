package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Der {@code LavaBlock} repräsentiert eine tödliche, animierte Umweltgefahr.
 * Wenn der Spieler mit diesem Block kollidiert (überlappt), erleidet der Spieler wiederkehrenden Schaden.
 *
 * <p>
 * Dieser Block erweitert {@code AnimatedBlock}, um eine fließende Lava-Textur anzuzeigen.
 * Die Kollisionslogik ist speziell darauf ausgelegt, Schaden zu umgehen, wenn der God Mode aktiv ist.
 *
 * @see AnimatedBlock
 * @see Material#LAVA
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class LavaBlock extends AnimatedBlock {
    /**
     * Konstruiert einen neuen {@code LavaBlock} an der angegebenen Position.
     * <p>
     * Initialisiert den Block mit den Animations-Frames für Lava und setzt seinen Materialtyp.
     *
     * @param location Die Weltposition (obere linke Ecke), an der der Block platziert werden soll.
     */
    public LavaBlock(Location location) {
        super(location, new String[] {
                "/assets/lavaset/lava_1.png",
                "/assets/lavaset/lava_2.png",
                "/assets/lavaset/lava_3.png"
        });
        this.setMaterial(Material.LAVA);
    }

    /**
     * Behandelt die Kollisionslogik, wenn eine Spieler-Entität mit diesem Block überlappt,
     * was zu sofortigem Tod führt, falls der God Mode nicht aktiv ist.
     * <p>
     * Wenn der Spieler sich im God Mode befindet, wird die Kollision ignoriert. Andernfalls
     * wird die Gesundheit des Spielers sofort auf Null gesetzt, wodurch die Todessequenz
     * ausgelöst wird (die an anderer Stelle behandelt wird).
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    @Override
    public void onCollide(Player player) {
        // Lava darf im GodMode keinen Schaden anrichten.
        if (player.isGodModeEnabled()) {
            return;
        }
        player.setHealth(0.0F);
    }
}