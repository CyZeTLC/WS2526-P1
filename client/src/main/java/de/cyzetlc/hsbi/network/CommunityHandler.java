package de.cyzetlc.hsbi.network;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.screens.CommunityScreen;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.level.impl.CommunityLevel;
import de.cyzetlc.hsbi.game.network.packets.ClientDataPacket;
import de.cyzetlc.hsbi.game.world.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

public class CommunityHandler {
    public static ArrayList<EntityPlayer> players = new ArrayList<>();

    public static void addPlayer(EntityPlayer entityPlayer) {
        players.add(entityPlayer);

        if (Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen
                && Game.getInstance().getCurrentLevel() instanceof CommunityLevel) {
            ((CommunityScreen) Game.getInstance().getScreenManager().getCurrentScreen()).addPlayer(entityPlayer);
        }
    }

    public static void updatePlayerData(ClientDataPacket dataPacket) {
        if (getPlayerByUUID(dataPacket.getUuid()) == null) {
            EntityPlayer player = new EntityPlayer();
            player.setUuid(dataPacket.getUuid());
            player.setLocation(dataPacket.getLocation());
            addPlayer(player);
        }

        for (EntityPlayer player : players) {
            if (player.getUuid() == dataPacket.getUuid()) {
                player.setLocation(dataPacket.getLocation());
            }
        }
    }

    public static EntityPlayer getPlayerByUUID(UUID uuid) {
        for (EntityPlayer player : players) {
            if (uuid == player.getUuid()) return player;
        }
        return null;
    }
}
