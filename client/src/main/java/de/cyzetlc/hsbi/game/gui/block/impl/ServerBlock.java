package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.world.Location;

/**
 * Der {@code ServerBlock} repräsentiert einen statischen, animierten Server- oder Datenblock,
 * der als visuelles Element oder als statische Plattform dienen kann.
 * <p>
 * Er nutzt eine einfache 2-Frame-Animation, um einen "aktiven" oder blinkenden Zustand darzustellen.
 *
 * @see AnimatedBlock
 * @see Material#SERVER
 *
 * @author Tom Coombs
 */
public class ServerBlock extends AnimatedBlock {
    /**
     * Konstruiert einen neuen {@code ServerBlock} an der angegebenen Position.
     * <p>
     * Initialisiert den Block mit zwei Frames für eine einfache Animation (Standard und beleuchtet)
     * und setzt seine Größe (64x128).
     *
     * @param location Die Weltposition, an der der Block platziert werden soll.
     */
    public ServerBlock(Location location) {
        super(location, new String[] {
                "/assets/hud/PlatformServerMehreZustände/Zugeschnitten1.png",
                "/assets/hud/PlatformServerMehreZustände/Zugeschnitten1 - Lichtzustand1.png"
        });
        this.setMaterial(Material.SERVER);
        this.setWidth(64);
        this.setHeight(128);
    }

    /**
     * Behandelt die Kollisionslogik, wenn ein Spieler diesen Block berührt.
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    @Override
    public void onCollide(Player player) {

    }
}