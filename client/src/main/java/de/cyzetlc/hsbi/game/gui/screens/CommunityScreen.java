package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Entity;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.level.impl.CommunityLevel;
import de.cyzetlc.hsbi.game.network.packets.ClientDataPacket;
import de.cyzetlc.hsbi.game.network.packets.JoinCommunityPacket;
import de.cyzetlc.hsbi.game.network.packets.PlayerListPacket;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.network.CommunityHandler;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommunityScreen extends GameScreen {
    public static ArrayList<EntityPlayer> players = new ArrayList<>();

    /**
     * Constructs a new CommunityScreen.
     *
     * @param screenManager The screen manager instance responsible for handling screen transitions.
     */
    public CommunityScreen(ScreenManager screenManager) {
        super(screenManager);

        try {
            Game.getInstance().getClient().sendPacket(new JoinCommunityPacket(Game.thePlayer.getUuid()));
        } catch (IOException e) {
            Game.getLogger().error(e.getMessage());
            return;
        }
        Game.getInstance().setCurrentLevel(new CommunityLevel());
    }

    @Override
    public void initialize() {
        super.initialize();
        double height = screenManager.getStage().getScene().getHeight();
        double width = screenManager.getStage().getScene().getWidth();

        UIUtils.drawCenteredText(root, "Community", 0, 65, false).setId("menu-title");

        /*for (ClientDataPacket dataPacket : this.playerListPacket.getDataPackets()) {
            EntityPlayer player = new EntityPlayer();
            player.setLocation(dataPacket.getLocation());
            player.setDisplayName(dataPacket.getName());
            player.setUuid(dataPacket.getUuid());

            if (player.getUuid() != Game.thePlayer.getUuid()) {
                this.players.add(player);
                player.drawPlayer(root, player.getLocation().getX() - getCameraX(), player.getLocation().getY() - getCameraY());
            }
        }*/
    }

    @Override
    public void update(double delta) {
        super.update(delta);
        for (EntityPlayer player : players) {
            if (player.getSprite() != null && player.getLocation() != null) {
                player.update();
            }
        }
    }

    public void addPlayer(EntityPlayer player) {
        players.add(player);
        player.drawPlayer(root, player.getLocation().getX() - getCameraX(), player.getLocation().getY() - getCameraY());
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "Community";
    }
}
