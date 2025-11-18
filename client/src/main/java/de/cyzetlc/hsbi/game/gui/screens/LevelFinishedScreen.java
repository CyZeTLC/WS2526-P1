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

public class LevelFinishedScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    public LevelFinishedScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @Override
    public void initialize() {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        SoundManager.playBackground(Music.MENU, true);

        UIUtils.drawImage(root, "/assets/hud/background.png", 0, 0, width, height);

        UIUtils.drawRect(root, width/2 - 300, height/2 - 300, 600, 600, Color.BLACK).setOpacity(0.4);
        UIUtils.drawCenteredText(root, "Level geschafft!", 0, 50, false).setId("menu-title");
        UIUtils.drawCenteredButton(root, "Nächstes Level", 0, height / 2 + 150, false, "mainmenu-button", () -> {
            Game.getInstance().setCurrentLevel(new SecondLevel());
            Game.getInstance().getScreenManager().showScreen(new GameScreen(Game.getInstance().getScreenManager()));
        });
        UIUtils.drawCenteredButton(root, "Zum Hauptmenü", 0, height / 2 + 230, false, "mainmenu-button", () -> {
            Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
        });

        UIUtils.drawCenteredText(root, Game.getInstance().getCurrentLevel().getName() + " abgeschlossen", 0, 300, false, "stats-line-title");
        UIUtils.drawCenteredText(root, "Benötigte Zeit: " + 125 + " Sekunden", 0, 380, false, "stats-line");
        UIUtils.drawCenteredText(root, "Punkte: " + 345, 0, 420, false, "stats-line");
        UIUtils.drawCenteredText(root, "Gegner getötet: " + 2, 0, 460, false, "stats-line");
        UIUtils.drawCenteredText(root, "Leben verloren: " + 0, 0, 500, false, "stats-line");

        UIUtils.drawText(root, "© Copyright CyZeTLC.DE & Phantomic", 10, height-20);
        UIUtils.drawText(root, "Steal The Files v0.1 (BETA)", width-210, height-20);

    }

    @Override
    public void update(double delta) {

    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "LevelFinished";
    }
}
