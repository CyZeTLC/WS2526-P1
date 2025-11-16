package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Music;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.FloatingPlatformBlock;
import de.cyzetlc.hsbi.game.gui.block.JumpBoostBlock;
import de.cyzetlc.hsbi.game.gui.block.LavaBlock;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Direction;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.scene.control.Button;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    private EntityPlayer player;
    private double dx = 1; // movement in X direction
    private double dy = 0.5; // movement in Y direction

    private final Text debugLbl;
    private final Text healthLbl;
    private Text volumeLbl;
    private Button muteBtn;
    private int volumeStep = 5; // 0-5 => 0-100%

    private final List<Platform> platforms = new ArrayList<>();
    private final List<Block> blocks = new ArrayList<>();

    private boolean gameOverTriggered = false;

    public GameScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        this.setupBackgroundVideo(width, height);
        SoundManager.playBackground(Music.GAME, true);
        player = Game.thePlayer;
        if (player.getHealth() <= 0) {
            player.setHealth(1.0F);
        }
        player.drawPlayer(root, 20, height - 450);

        // back to menu
        UIUtils.drawButton(root, "Zurueck", 10, 10, () -> screenManager.showScreen(new MainMenuScreen(screenManager)));

        this.debugLbl = UIUtils.drawText(root, "FPS: " + screenManager.getCurrentFps(), 10, 85);
        this.healthLbl = UIUtils.drawText(root, "HP: 100%", 10, 105);
        this.setupSoundControls(width);

        // example platforms (placeholder layout)
        platforms.add(new Platform(0, height - 300, 450, 300, root));
        platforms.add(new Platform(500, height - 350, 200, 350, root));
        platforms.add(new Platform(780, height - 300, 150, 300, root));

        this.blocks.add(new JumpBoostBlock(new Location(150, height - 332)));

        // floating platform connects upper islands
        this.blocks.add(new FloatingPlatformBlock(
                new Location(420, height - 420),
                new Location(720, height - 420),
                120
        ));

        // draw blocks
        this.fillGapsWithLava(height);
        for (Block block : this.blocks) {
            block.draw(root);
        }
    }

    @Override
    public void update(double delta) {
        if (player.getHealth() <= 0) {
            this.handleGameOver();
            return;
        }

        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        // move dynamic blocks (e.g. floating platform) before collision checks
        for (Block block : this.blocks) {
            block.update();
        }

        double gravity = Game.gravity;       // gravity strength
        double moveSpeed = Game.moveSpeed;    // horizontal speed
        double jumpPower = Game.jumpPower;    // jump power
        boolean onGround = false;

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();

        // input
        if (screenManager.getInputManager().isPressed(KeyCode.A)) {
            dx = -moveSpeed * delta;
            player.setDirection(Direction.WALK_LEFT);
        } else if (screenManager.getInputManager().isPressed(KeyCode.D)) {
            dx = moveSpeed * delta;
            player.setDirection(Direction.WALK_RIGHT);
        } else {
            dx = 0;
        }

        // jump (only if on ground)
        if (screenManager.getInputManager().isPressed(KeyCode.SPACE) && dy == 0) {
            dy = -jumpPower * delta;
            player.setDirection(Direction.JUMP);
        }

        // gravity
        dy += gravity * delta;

        // tentative position
        double nextX = x + dx;
        double nextY = y + dy;

        Rectangle2D nextBounds = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());

        // platform collisions
        for (Platform platform : platforms) {
            Rectangle2D pBounds = platform.getBounds();

            if (nextBounds.intersects(pBounds)) {
                // landing from above
                if (y + player.getHeight() <= platform.getY()) {
                    nextY = platform.getY() - player.getHeight();
                    dy = 0;
                    onGround = true;
                }
                // side collision
                else if (x + player.getWidth() <= platform.getX()) {
                    nextX = platform.getX() - player.getWidth();
                    dx = 0;
                }
                else if (x >= platform.getX() + platform.getWidth()) {
                    nextX = platform.getX() + platform.getWidth();
                    dx = 0;
                }
            }
        }

        // block collisions
        for (Block block : this.blocks) {
            Rectangle2D pBounds = block.getBounds();

            if (nextBounds.intersects(pBounds) && block.isActive()) {
                block.onCollide(player);

                if (block.isCollideAble()) {
                    // landing from above
                    if (y + player.getHeight() <= block.getLocation().getY()) {
                        nextY = block.getLocation().getY() - player.getHeight();
                        nextX += block.getDeltaX(); // follow moving platform x movement
                        dy = 0;
                        onGround = true;
                    }
                    // left
                    else if (x + player.getWidth() <= block.getLocation().getX()) {
                        nextX = block.getLocation().getX() - player.getWidth();
                        dx = 0;
                    }
                    // right
                    else if (x >= block.getLocation().getX() + block.getWidth()) {
                        nextX = block.getLocation().getX() + block.getWidth();
                        dx = 0;
                    }
                }
            }
        }

        // window bounds
        if (nextX < 0) nextX = 0;
        if (nextX + player.getWidth() > width) nextX = width - player.getWidth();

        // fell out of the world -> game over
        if (nextY + player.getHeight() > height) {
            if (player.getHealth() > 0) {
                player.setHealth(0);
            }
            this.handleGameOver();
            return;
        }

        // apply position
        player.getLocation().setX(nextX);
        player.getLocation().setY(nextY);

        // debug info
        this.debugLbl.setText("FPS: " + (int) screenManager.getCurrentFps() +
                " | onGround: " + onGround +
                " | moveSpeed: " + moveSpeed +
                " | jumpPower: " + jumpPower +
                " | uuid: " + player.getUuid());
        int hpPercent = (int) Math.round(player.getHealth() / player.getMaxHealth() * 100.0);
        this.healthLbl.setText("HP: " + hpPercent + "%");

        player.update();
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public String getName() {
        return "GameScreen";
    }

    private void handleGameOver() {
        if (this.gameOverTriggered) {
            return;
        }
        this.gameOverTriggered = true;
        if (!(screenManager.getCurrentScreen() instanceof MainMenuScreen)) {
            this.screenManager.showScreen(new MainMenuScreen(screenManager));
        }
    }

    private void setupBackgroundVideo(double width, double height) {
        String videoPath = "client/src/main/java/de/cyzetlc/hsbi/game/gui/screens/Loop Matrix Desktop Wallpaper Full HD (1080p_30fps_H264-128kbit_AAC).mp4";
        File file = new File(videoPath);
        if (!file.exists()) {
            UIUtils.drawRect(root, 0, 0, width, height, Color.LIGHTBLUE);
            return;
        }

        try {
            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            MediaView view = new MediaView(player);
            view.setFitWidth(width);
            view.setFitHeight(height);
            view.setPreserveRatio(false);
            root.getChildren().add(view); // add first so other nodes render above
            player.play();
        } catch (Exception e) {
            System.err.println("Could not load video: " + videoPath);
            e.printStackTrace();
            UIUtils.drawRect(root, 0, 0, width, height, Color.LIGHTBLUE);
        }
    }

    private void setupSoundControls(double width) {
        double panelWidth = 220;
        double panelHeight = 90;
        double x = width - panelWidth - 20;
        double y = 20;

        UIUtils.drawRect(root, x, y, panelWidth, panelHeight, Color.BLACK).setOpacity(0.55);
        Text title = UIUtils.drawText(root, "Sound", x + 10, y + 22);
        title.setFill(Color.WHITE);

        this.volumeStep = (int) Math.round(SoundManager.getVolume() * 5);
        this.volumeLbl = UIUtils.drawText(root, "", x + 10, y + 45);
        this.volumeLbl.setFill(Color.WHITE);

        UIUtils.drawButton(root, "-", x + 10, y + 60, () -> changeVolume(-1));
        UIUtils.drawButton(root, "+", x + 50, y + 60, () -> changeVolume(1));
        this.muteBtn = UIUtils.drawButton(root, "", x + 90, y + 60, this::toggleMute);

        this.updateVolumeLabel();
        this.updateMuteButton();
    }

    private void changeVolume(int delta) {
        this.volumeStep = Math.max(0, Math.min(5, this.volumeStep + delta));
        double newVolume = this.volumeStep / 5.0;
        SoundManager.setVolume(newVolume);
        if (SoundManager.isMuted() && newVolume > 0) {
            SoundManager.setMuted(false);
        }
        this.updateVolumeLabel();
        this.updateMuteButton();
    }

    private void toggleMute() {
        SoundManager.setMuted(!SoundManager.isMuted());
        this.updateMuteButton();
        this.updateVolumeLabel();
    }

    private void updateVolumeLabel() {
        int percent = (int) Math.round(SoundManager.getVolume() * 100);
        String muteSuffix = SoundManager.isMuted() ? " (stumm)" : "";
        this.volumeLbl.setText("Lautstaerke: " + percent + "% (Stufe " + this.volumeStep + "/5)" + muteSuffix);
    }

    private void updateMuteButton() {
        this.muteBtn.setText(SoundManager.isMuted() ? "Sound AN" : "Mute");
    }

    private void fillGapsWithLava(double screenHeight) {
        // gap between platform 1 (0-450) and 2 (500-700)
        this.blocks.add(createLavaColumn(450, screenHeight - 300, 50, 300));

        // gap between platform 2 (500-700) and 3 (780-930)
        this.blocks.add(createLavaColumn(700, screenHeight - 350, 80, 350));
    }

    private static LavaBlock createLavaColumn(double x, double y, double width, double height) {
        LavaBlock lava = new LavaBlock(new Location(x, y));
        lava.setWidth(width);
        lava.setHeight(height);
        return lava;
    }
}
