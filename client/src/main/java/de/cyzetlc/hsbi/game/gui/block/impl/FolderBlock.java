package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Sound;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.block.PerkBlock;
import de.cyzetlc.hsbi.game.world.Location;

/**
 * Der {@code FolderBlock} repräsentiert ein einsammelbares Objekt im Spiel, das eine
 * Datei symbolisiert, die der Spieler erwerben muss.
 * <p>
 * Dieser Block kann nur eingesammelt werden und löst einen Effekt aus, wenn der Spieler
 * sich im korrekten Zustand zum Sammeln von Dateien befindet (geprüft über {@code Game.thePlayer.isCanCollectFiles()}).
 *
 * @see PerkBlock
 * @see Material#FOLDER_ITEM
 *
 * @author Tom Coombs
 */
public class FolderBlock extends PerkBlock {

    /**
     * Konstruiert einen neuen {@code FolderBlock} an der angegebenen Position.
     * <p>
     * Initialisiert den Block mit dem Material {@code FOLDER_ITEM} und setzt ihn als
     * nicht kollidierbar, was bedeutet, dass der Spieler ihn überlappen kann, um
     * das Einsammelevent auszulösen.
     *
     * @param location Die Weltposition, an der der Block platziert werden soll.
     */
    public FolderBlock(Location location) {
        super(location);
        this.setMaterial(Material.FOLDER_ITEM);
        this.setCollideAble(false);
    }

    /**
     * Behandelt die Einsammel-Logik, wenn eine Spieler-Entität den {@code FolderBlock} überlappt.
     * <p>
     * Der Block wird nur dann eingesammelt (und dadurch deaktiviert), wenn das {@code canCollectFiles}-Flag
     * des Spielers auf {@code true} gesetzt ist. Falls eingesammelt, wird ein Klick-Sound abgespielt.
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    @Override
    public void onCollide(Player player) {
        if (Game.thePlayer.isCanCollectFiles()) {
            this.setActive(false);
            SoundManager.play(Sound.CLICK);
        }
    }
}