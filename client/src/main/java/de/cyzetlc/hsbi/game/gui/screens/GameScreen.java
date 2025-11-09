package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class GameScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    private EntityPlayer player;
    private double dx = 1; // Bewegung in X-Richtung
    private double dy = 0.5; // Bewegung in Y-Richtung

    private final Text fpsLbl;

    public GameScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;

        // Beispiel: Spielfigur
        player = Game.thePlayer;
        player.drawPlayer(root, 20, 20);

        // Zurück zum Menü
        Button backButton = new Button("Zurück");
        backButton.setLayoutX(10);
        backButton.setLayoutY(10);
        backButton.setOnAction(e -> screenManager.showScreen(new MainMenuScreen(screenManager)));

        this.fpsLbl = UIUtils.drawText(root, "FPS: " + screenManager.getCurrentFps(), 10, 85);

        root.getChildren().add(backButton);
    }

    @Override
    public void update(double delta) {
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        // einfache Bewegung
        player.getLocation().setX(player.getLocation().getX() + dx);
        player.getLocation().setY(player.getLocation().getY() + dy);

        if (screenManager.getInputManager().isPressed(KeyCode.W)) {
            dy -= 4 * delta;
            dy = -2.5;
            dx = 0;
        } else if (screenManager.getInputManager().isPressed(KeyCode.S)) {
            dy += 4 * delta;
            dy = 2.5;
            dx = 0;
        } else if (screenManager.getInputManager().isPressed(KeyCode.D)) {
            dx += 4 * delta;
            dx = 2.5;
            dy = 0;
        } else if (screenManager.getInputManager().isPressed(KeyCode.A)) {
            dx -= 4 * delta;
            dx = -2.5;
            dy = 0;
        } else {
            if (dx > 0) dx = 1;
            else dx = -1;

            if (dy > 0) dy = 0.5;
            else dy = -0.5;
        }

        if (screenManager.getInputManager().isPressed(KeyCode.F3) && screenManager.getInputManager().isPressed(KeyCode.R)) {
            player.setLocation(new Location(width/2-10,height/2-10));
        }

        // Kollision mit Rand
        if (player.getLocation().getX() <= 20 || player.getLocation().getX() >= width-20) dx *= -1;
        else
        if (player.getLocation().getY() <= 20 || player.getLocation().getY() >= height-20) dy *= -1;

        this.fpsLbl.setText("FPS: " + (int) screenManager.getCurrentFps());
        this.player.update();
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "GameScreen";
    }
}

