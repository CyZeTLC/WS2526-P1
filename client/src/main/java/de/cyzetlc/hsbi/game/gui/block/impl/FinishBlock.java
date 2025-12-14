package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.gui.screens.LevelFinishedScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.level.impl.SecondLevel;
import de.cyzetlc.hsbi.game.level.impl.TutorialLevel;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Der {@code FinishBlock} repräsentiert das Endziel oder den Ausgangspunkt eines Levels.
 * Wenn der Spieler mit diesem Block kollidiert, gilt das aktuelle Level als abgeschlossen,
 * was die Anzeige des {@code LevelFinishedScreen} auslöst und den Übergang zum nächsten
 * Level oder das Zurücksetzen des Spielzustands behandelt, falls alle Level abgeschlossen sind.
 * <p>
 * Dieser Block ist visuell animiert und nicht kollidierbar (löst nur einen Effekt beim Überlappen aus).
 *
 *
 * @see AnimatedBlock
 * @see LevelFinishedScreen
 *
 * @author Tom Coombs
 * @author Leonardo Parrino
 */
public class FinishBlock extends AnimatedBlock {

    /**
     * Konstruiert einen neuen {@code FinishBlock} an der angegebenen Position.
     * <p>
     * Initialisiert den Block mit einem Satz von Animations-Frames, setzt sein Material,
     * deaktiviert die Kollision und definiert seine Größe.
     *
     * @param location Die Weltposition, an der der Block platziert werden soll.
     */
    public FinishBlock(Location location) {
        super(location, new String[] {
                "/assets/USB-Stick/PortalTutorial/Portal1-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal2-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal3-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal4-removebg-preview.png",
                "/assets/USB-Stick/PortalTutorial/Portal5-removebg-preview.png"
        });
        this.setMaterial(Material.FINISH_FLAG);
        this.setCollideAble(false);
        this.setWidth(90);
        this.setHeight(90);
    }

    /**
     * Behandelt die Logik, die ausgeführt wird, wenn eine Spieler-Entität mit dem {@code FinishBlock}
     * kollidiert (überlappt).
     * <p>
     * Diese Methode führt die folgenden kritischen Aktionen aus:
     * <ul>
     * <li>Ruft {@code onFinish()} für das aktuelle Level auf.</li>
     * <li>Zeigt den {@code LevelFinishedScreen} an.</li>
     * <li>Bestimmt das nächste Level basierend auf {@code getCurrentLevel().getNextLevel()}.</li>
     * <li>Speichert den Namen des nächsten Levels in der Konfiguration oder setzt auf "Tutorial" zurück,
     * wenn das Spiel abgeschlossen ist.</li>
     * </ul>
     *
     * @param player Die {@code Player}-Instanz, die mit dem Block kollidiert ist.
     */
    @Override
    public void onCollide(Player player) {
        Game.getLogger().info(Game.getInstance().getCurrentLevel().getName() + " finished!");
        Game.getInstance().getCurrentLevel().onFinish();
        Game.getInstance().getScreenManager().showScreen(new LevelFinishedScreen(Game.getInstance().getScreenManager()));

        if (Game.getInstance().getCurrentLevel().getNextLevel() != null) {
            Game.getLogger().info("Loading & saving next level..");
            Game.getInstance().getConfig().getObject().put("currentLevel", Game.getInstance().getCurrentLevel().getNextLevel().getName());
            Game.getInstance().getConfig().save();
        } else {
            Game.getLogger().info("Game successfully finished (no more levels left)!");
            Game.getInstance().setCurrentLevel(new TutorialLevel());
            Game.getInstance().getConfig().getObject().put("currentLevel", "Tutorial");
            Game.getInstance().getConfig().save();
        }
    }
}