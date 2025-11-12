package de.cyzetlc.hsbi.game.gui.block;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;

public class LavaBlock extends Block {
    public LavaBlock(Location location) {
        super(location);
        this.setMaterial(Material.LAVA);
    }

    @Override
    public void onCollide(Player player) {
        // Spieler töten
        player.setHealth(0.0F);

        // Spieler zurück ins MainMenu senden
        Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
    }

    @Override
    public void update() {
        //Animation der Lava
    }
}
