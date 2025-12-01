package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Music;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.level.impl.SecondLevel;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * The {@code LevelFinishedScreen} displays a summary screen when the player successfully
 * completes a game level.
 * <p>
 * This screen shows the player's performance statistics (time taken, score, enemies defeated,
 * health lost) and provides options to proceed to the next level or return to the main menu.
 *
 * @author Tom Coombs
 * @author Leonardo (aka. Phantomic)
 *
 * @see GuiScreen
 * @see ScreenManager
 */
public class LevelFinishedScreen implements GuiScreen {
    /**
     * The root container for all visual elements displayed on this screen.
     */
    private final Pane root = new Pane();

    /**
     * Reference to the ScreenManager, used for handling screen transitions.
     */
    private final ScreenManager screenManager;

    /**
     * Constructs a new LevelFinishedScreen.
     *
     * @param screenManager The screen manager instance responsible for handling screen transitions.
     */
    public LevelFinishedScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    /**
     * Initializes the Level Finished screen, sets up the background, title, statistics,
     * and navigation buttons.
     * <p>
     * This method calculates the required time taken (minutes and seconds) by comparing
     * the current time against the level's start time, and displays statistics
     * for points, enemies killed, and health lost.
     */
    @Override
    public void initialize() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");

        UIUtils.drawRect(root, width/2 - 300, height/2 - 300, 600, 600, Color.BLACK).setOpacity(0.4);
        UIUtils.drawCenteredText(root, "Level geschafft!", 0, 50, false).setId("menu-title");

        if (Game.getInstance().getCurrentLevel().getNextLevel() != null) {
            UIUtils.drawCenteredButton(root, "Nächstes Level", 0, height / 2 + 150, false, "mainmenu-button", () -> {
                Game.getInstance().setCurrentLevel(Game.getInstance().getCurrentLevel().getNextLevel());
                Game.getInstance().getScreenManager().showScreen(new GameScreen(Game.getInstance().getScreenManager()));
                Game.getLogger().info(Game.getInstance().getCurrentLevel().getName() + " successfully loaded & saved!");
            });
        }
        UIUtils.drawCenteredButton(root, "Zum Hauptmenü", 0, height / 2 + 230, false, "mainmenu-button", () -> {
            Game.getInstance().getScreenManager().showScreen(Game.getInstance().getMainMenuScreen());
        });

        long millis = System.currentTimeMillis() - Game.getInstance().getCurrentLevel().getLevelStarted();
        long secs = millis / 1000;
        long mins = secs / 60;
        long restsecs = secs % 60;

        UIUtils.drawCenteredText(root, Game.getInstance().getCurrentLevel().getName() + " abgeschlossen", 0, 300, false, "stats-line-title");
        UIUtils.drawCenteredText(root, "Benötigte Zeit: " + mins + ":" + restsecs, 0, 380, false, "stats-line");
        UIUtils.drawCenteredText(root, "Punkte: " + 345, 0, 420, false, "stats-line");
        UIUtils.drawCenteredText(root, "Gegner getötet: " + 2, 0, 460, false, "stats-line");
        UIUtils.drawCenteredText(root, "Leben verloren: " + 0, 0, 500, false, "stats-line");

        UIUtils.drawText(root, "© Copyright CyZeTLC.DE & Phantomic", 10, height-20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width-210, height-20);

    }

    /**
     * Retrieves the root pane of the LevelFinishedScreen.
     *
     * @return The JavaFX {@code Pane} used as the root container.
     */
    @Override
    public Pane getRoot() {
        return root;
    }

    /**
     * Returns the identifying name of this screen.
     *
     * @return The constant screen name "LevelFinished".
     */
    @Override
    public String getName() {
        return "LevelFinished";
    }
}
