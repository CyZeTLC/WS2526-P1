package de.cyzetlc.hsbi.game.listener;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.event.EventHandler;
import de.cyzetlc.hsbi.game.event.impl.damage.PlayerDamageEvent;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;

public class PlayerListener {
    @EventHandler
    public void handleDamage(PlayerDamageEvent e) {
        if (e.getNewHealth() == 0) {
            Game.getInstance().setBackScreen(null);
            Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
            Game.thePlayer.setCanCollectFiles(false);
        }
    }
}
