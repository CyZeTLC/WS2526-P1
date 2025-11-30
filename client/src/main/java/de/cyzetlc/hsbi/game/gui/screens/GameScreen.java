package de.cyzetlc.hsbi.game.gui.screens;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.audio.Music;
import de.cyzetlc.hsbi.game.audio.SoundManager;
import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.gui.GuiScreen;
import de.cyzetlc.hsbi.game.gui.Platform;
import de.cyzetlc.hsbi.game.gui.ScreenManager;
import de.cyzetlc.hsbi.game.gui.block.Block;
import de.cyzetlc.hsbi.game.gui.block.impl.LaserBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.GasBarrierBlock;
import de.cyzetlc.hsbi.game.gui.block.impl.RobotEnemyBlock;
import de.cyzetlc.hsbi.game.level.impl.TutorialLevel;
import de.cyzetlc.hsbi.game.utils.ui.UIUtils;
import de.cyzetlc.hsbi.game.world.Direction;
import javafx.scene.control.Button;
import javafx.geometry.Rectangle2D;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements GuiScreen {
    private final Pane root = new Pane();
    private final ScreenManager screenManager;

    private EntityPlayer player;
    private double dx = 1; // movement in X direction
    private double dy = 0.5; // movement in Y direction

    private Text debugLbl;
    private Text healthLbl;
    private Text volumeLbl;
    private Button muteBtn;
    private int volumeStep = 5; // 0-5 => 0-100%

    private boolean paused = false;
    private Pane pauseOverlay;

    private boolean gameOverTriggered = false;

    @Getter
    private double cameraX = 0;

    @Getter
    private double cameraY = 0;

    private final double cameraSmooth = 0.1; // wie schnell die Kamera folgt

    private final double marginX = 400;
    private final double marginY = 150;

    private Text flipperHint;
    private boolean flipperHintShown = false;

    private Text questLbl;
    private Text filesProgressLbl;
    private int totalFolderCount = 0;

    // HUD/Debug toggles
    private boolean showTooltips = true; // F1
    private boolean showDebugBar = true; // F2
    private Text debugBarLbl;
    private Text tipsLbl;
    private Text debugStatusLbl;
    private boolean lastF1 = false;
    private boolean lastF2 = false;
    private boolean lastF3 = false;

    public GameScreen(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @Override
    public void initialize() {
        root.getChildren().clear();

        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();
        this.player = Game.thePlayer;

        //this.setupBackgroundVideo(width, height);
        UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                "/assets/hud/BackgroundZustand1.png",
                "/assets/hud/BackgroundZustand2.png");

        if (player.getHealth() <= 0) {
            player.setHealth(player.getMaxHealth());
        }
        player.drawPlayer(root, 20 - cameraX,
                height - 450 - cameraY);

        Game.getInstance().getCurrentLevel().getBlocks().clear();
        Game.getInstance().getCurrentLevel().getPlatforms().clear();
        Game.getInstance().getCurrentLevel().draw(width, height, root);

        // back to menu
        UIUtils.drawButton(root, "Zurück", 10, 10, () -> screenManager.showScreen(new MainMenuScreen(screenManager)));
        UIUtils.drawButton(root, "Pause", 150, 10, this::togglePause);

        this.debugLbl = UIUtils.drawText(root, "FPS: " + screenManager.getCurrentFps(), 10, 85);
        this.healthLbl = UIUtils.drawText(root, "HP: 100%", 10, 105);
        this.debugLbl.setVisible(false); // alte Debug-Anzeige verstecken, ersetzen wir durch debugBarLbl
        this.healthLbl.setVisible(false); // HP kommt in der neuen Debug-Bar/Quest-Anzeige vor
        //this.setupSoundControls(width);
        this.setupPauseOverlay(width, height);
        this.flipperHint = UIUtils.drawText(root, "", 10, 135);
        this.flipperHint.setVisible(false);

        this.totalFolderCount = this.countFolderBlocks();
        this.questLbl = UIUtils.drawText(root, "Quest: Sammel alle Files mit deinem USB Stick", 10, 0);
        this.filesProgressLbl = UIUtils.drawText(root, "", 10, 0);
        this.updateFolderProgress();

        // HUD-Hinweise und Debug-Status (F1/F2/F3) in Giftgrün, Positionierung erfolgt zentral
        this.debugBarLbl = UIUtils.drawText(root, "", 10, 0);
        this.debugBarLbl.setFill(Color.rgb(80, 255, 80));
        this.debugStatusLbl = UIUtils.drawText(root, "DEBUG: AUS", 10, 0);
        this.debugStatusLbl.setFill(Color.rgb(80, 255, 80));
        this.tipsLbl = UIUtils.drawText(root, "[E] SchrankeGas deaktivieren (nur mit Flipper) | [F1] Tooltips | [F2] Debug-Leiste | [F3] NoClip+GodMode", 10, 0);
        this.tipsLbl.setFill(Color.rgb(80, 255, 80));
        this.debugBarLbl.setVisible(showDebugBar);
        this.debugStatusLbl.setVisible(showTooltips);
        this.tipsLbl.setVisible(showTooltips);

        // Gemeinsame HUD-Positionierung (unter Buttons, fester Zeilenabstand, keine Überlappungen)
        layoutHudPositions();

        this.drawHealth(width);
    }

    @Override
    public void update(double delta) {
        if (player.getHealth() <= 0) {
            this.handleGameOver();
            return;
        }

        double width = screenManager.getStage().getWidth();
        double height = screenManager.getStage().getHeight();

        double gravity = Game.gravity;       // gravity strength
        double moveSpeed = Game.moveSpeed;    // horizontal speed
        double jumpPower = Game.jumpPower;    // jump power
        boolean onGround = false;
        boolean hittingCeiling = false;
        boolean interactPressed = screenManager.getInputManager().isPressed(KeyCode.E);
        boolean f1 = screenManager.getInputManager().pollJustPressed(KeyCode.F1);
        boolean f2 = screenManager.getInputManager().pollJustPressed(KeyCode.F2);
        boolean f3 = screenManager.getInputManager().pollJustPressed(KeyCode.F3);

        // Tooltips toggeln (F1)
        if (f1) {
            showTooltips = !showTooltips;
            tipsLbl.setVisible(showTooltips);
            debugStatusLbl.setVisible(showTooltips);
            questLbl.setVisible(showTooltips);
            filesProgressLbl.setVisible(showTooltips);
        }

        // Debug-Leiste toggeln (F2)
        if (f2) {
            showDebugBar = !showDebugBar;
            debugBarLbl.setVisible(showDebugBar);
        }

        // NoClip + GodMode toggeln (F3)
        if (f3) {
            boolean enable = !player.isNoClipEnabled();
            player.setNoClip(enable);
            player.setGodMode(enable);
        }

        // Debug-Status-Text aktualisieren (anzeige nur wenn Tooltips an)
        if (showTooltips) {
            debugStatusLbl.setText(player.isNoClipEnabled() ? "DEBUG: NoClip+GodMode AN" : "DEBUG: AUS");
        }

        double x = player.getLocation().getX();
        double y = player.getLocation().getY();

        // NoClip: freie Bewegung ohne Gravitation/Kollision
        if (player.isNoClipEnabled()) {
            double freeSpeed = moveSpeed * 1.8 * delta;
            double nx = x;
            double ny = y;
            if (screenManager.getInputManager().isPressed(KeyCode.A)) nx -= freeSpeed;
            if (screenManager.getInputManager().isPressed(KeyCode.D)) nx += freeSpeed;
            if (screenManager.getInputManager().isPressed(KeyCode.W)) ny -= freeSpeed;
            if (screenManager.getInputManager().isPressed(KeyCode.S)) ny += freeSpeed;
            player.getLocation().setX(nx);
            player.getLocation().setY(ny);
            this.updateCamera(width, height);
            this.updateDebugBar(onGround, moveSpeed, jumpPower);
            this.updateFolderProgress();
            if (player.hasFlipper()) {
                flipperHint.setText("Flipper vorhanden: E zum Deaktivieren von Gas");
                flipperHint.setVisible(true);
            }
            // Sprite/Nametag auch im NoClip aktualisieren, damit er sichtbar mitfliegt
            player.update();
            return; // Skip Kollisionen/Gravitation
        }

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

        // gravity
        dy += gravity * delta;

        // tentative position
        double nextX = x + dx;
        double nextY = y + dy;

        Rectangle2D nextBounds = new Rectangle2D(nextX, nextY, player.getWidth(), player.getHeight());

        // platform collisions
        for (Platform platform : Game.getInstance().getCurrentLevel().getPlatforms()) {
            Rectangle2D pBounds = platform.getBounds();
            platform.update(this);

            if (nextBounds.intersects(pBounds)) {
                // landing from above
                if (y + player.getHeight() <= platform.getY()) {
                    nextY = platform.getY() - player.getHeight();
                    dy = 0;
                    onGround = true;
                }
                // collision from below
                if (dy < 0 && y >= platform.getY() + platform.getHeight() && nextY <= platform.getY() + platform.getHeight()) {
                    nextY = platform.getY() + platform.getHeight();
                    hittingCeiling = true;
                    dy = 0;
                } else {
                    hittingCeiling = false;
                }

                // side collision
                if (x + player.getWidth() <= platform.getX()) {
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
        List<Block> pendingBlocks = new ArrayList<>();
        List<Block> blocks = Game.getInstance().getCurrentLevel().getBlocks();
        for (Block block : blocks) {
            Rectangle2D pBounds = block.getBounds();
            // Flipper-Item-Logik: Einsammeln & HUD-Flag setzen
            if (block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FlipperItem flipperItem) {
                flipperItem.update(player);
            } else {
                block.update();
            }

            if (block instanceof RobotEnemyBlock enemy) {
                LaserBlock laser = enemy.tryFire(player);
                if (laser != null) {
                    laser.draw(root);
                    pendingBlocks.add(laser);
                }
            }

            if (nextBounds.intersects(pBounds) && block.isActive() && !player.isNoClipEnabled()) {
                if (block instanceof GasBarrierBlock barrier && interactPressed && player.hasFlipper()) {
                    barrier.deactivate();
                    continue;
                }
                if (block instanceof RobotEnemyBlock enemy) {
                    double enemyTop = enemy.getLocation().getY();
                    boolean stomp = (y + player.getHeight() <= enemyTop + 6) && dy > 0;
                    if (stomp) {
                        enemy.kill();
                        nextY = enemyTop - player.getHeight();
                        dy = -jumpPower * delta * 0.6;
                    } else {
                        enemy.hitPlayer(player);
                    }
                    // continue with collision resolution but skip duplicate onCollide
                } else {
                    block.onCollide(player);
                }

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
        if (!pendingBlocks.isEmpty()) {
            blocks.addAll(pendingBlocks);
        }

        // window bounds
        if (nextX < 0) nextX = 0;
        //if (nextX + player.getWidth() > width) nextX = width - player.getWidth();

        // fell out of the world -> game over
        double screenNextY = nextY - this.cameraY;
        if (screenNextY + player.getHeight() > height) {
            if (player.getHealth() > 0) {
                player.setHealth(0);
            }
            this.handleGameOver();
            return;
        }

        // jump (only if on ground)
        if (screenManager.getInputManager().isPressed(KeyCode.SPACE) && dy == 0 && !hittingCeiling) {
            dy = -jumpPower * delta;
            player.setDirection(Direction.JUMP);
        }

        // apply position
        player.getLocation().setX(nextX);
        player.getLocation().setY(nextY);

        this.updateCamera(width, height);

        this.updateDebugBar(onGround, moveSpeed, jumpPower);

        int hpPercent = (int) Math.round(player.getHealth() / player.getMaxHealth() * 100.0);
        this.healthLbl.setText("HP: " + hpPercent + "%");

        if (player.hasFlipper() && !flipperHintShown) {
            flipperHint.setText("Druecke E damit der Flipper mit Gegenstaenden interagiert");
            flipperHint.setVisible(true);
            flipperHintShown = true;
        }

        this.updateFolderProgress();

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

    private void updateCamera(double width, double height) {
        double playerScreenX = player.getLocation().getX() - this.cameraX;
        double playerScreenY = player.getLocation().getY() - this.cameraY;

        double targetCamX = this.cameraX;
        double targetCamY = this.cameraY;

        if (playerScreenX > width - this.marginX) {
            targetCamX += playerScreenX - (width - this.marginX);
        } else if (playerScreenX < this.marginX) {
            targetCamX -= (this.marginX - playerScreenX);
        }

        if (playerScreenY > height - this.marginY) {
            targetCamY += playerScreenY - (height - this.marginY);
        } else if (playerScreenY < this.marginY) {
            targetCamY -= (this.marginY - playerScreenY);
        }

        // sanftes Folgen (definiert durch cameraSmooth)
        this.cameraX += (targetCamX - this.cameraX) * this.cameraSmooth;
        this.cameraY += (targetCamY - this.cameraY) * this.cameraSmooth;

        // Kamera-Clamp nur im Normalmodus; im NoClip darf man frei fliegen
        if (!player.isNoClipEnabled()) {
            if (this.cameraX < 0) this.cameraX = 0;
            if (this.cameraY < 0) this.cameraY = 0;
        }
    }

    private void setupPauseOverlay(double width, double height) {
        this.pauseOverlay = new Pane();
        UIUtils.drawRect(pauseOverlay, 0, 0, width, height, Color.BLACK).setOpacity(0.5);
        Text title = UIUtils.drawCenteredText(pauseOverlay, "Pause", 0, height / 2 - 120, false);
        title.setFill(Color.WHITE);

        UIUtils.drawCenteredButton(pauseOverlay, "Weiter", 0, height / 2 - 50, false, this::togglePause);
        UIUtils.drawCenteredButton(pauseOverlay, "Zum Menu", 0, height / 2 + 20, false, () -> {
            this.paused = false;
            this.pauseOverlay.setVisible(false);
            screenManager.showScreen(new MainMenuScreen(screenManager));
        });

        this.pauseOverlay.setVisible(false);
        root.getChildren().add(pauseOverlay);
    }

    private void togglePause() {
        this.paused = !this.paused;
        if (this.pauseOverlay != null) {
            this.pauseOverlay.setVisible(this.paused);
        }
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
        String videoPath = "client/src/main/java/de/cyzetlc/hsbi/game/gui/screens/Loop Matrix Desktop Wallpaper Full HD (1080p_30fps_H264-128kbit_AAC).mp4d";
        File file = new File(videoPath);
        if (!file.exists()) {
            //UIUtils.drawRect(root, 0, 0, width, height, Color.rgb(28, 20, 20));
            UIUtils.drawAnimatedBackground(root, width, height, Duration.millis(900),
                    "/assets/hud/BackgroundZustand1.png",
                    "/assets/hud/BackgroundZustand2.png");
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
        double panelHeight = 120;
        double x = width - panelWidth - 20;
        double y = 80;

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

    private void drawHealth(double width) {
        int heartSize = 32;
        int padding = 4;
        int maxLives = (int) player.getMaxHealth();

        double lives = player.getHealth();

        int startX = (int) (width - (maxLives * (heartSize + padding)) - 16);

        for (int i = 0; i < maxLives; i++) {
            int x = startX + i * (heartSize + padding);

            if (lives >= i + 1) {
                UIUtils.drawImage(root, "/assets/hud/heart_full.png", x, 16, heartSize, heartSize);
            } else if (lives > i && lives < i + 1) {
                UIUtils.drawImage(root, "/assets/hud/heart_half.png", x, 16, heartSize, heartSize);

            } else {
                UIUtils.drawImage(root, "/assets/hud/heart_empty.png", x, 16, heartSize, heartSize);
            }
        }
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
        this.volumeLbl.setText("Lautstaerke: " + percent + "% (" + this.volumeStep + "/5)" + muteSuffix);
    }

    private void updateMuteButton() {
        this.muteBtn.setText(SoundManager.isMuted() ? "Sound AN" : "Mute");
    }

    /**
     * Setzt die Y-Positionen aller HUD-Texte konsistent unterhalb der Buttons.
     */
    private void layoutHudPositions() {
        int hudX = 10;
        int hudStartY = 140;    // weiter unter den Buttons, damit nichts überlappt
        int lineHeight = 18;    // fester Zeilenabstand
        int y = hudStartY;

        if (this.debugBarLbl != null) {
            this.debugBarLbl.setX(hudX);
            this.debugBarLbl.setY(y);
            // debugBarLbl enthält 3 Zeilen (per \n)
            y += lineHeight * 3;
        }
        if (this.debugStatusLbl != null) {
            this.debugStatusLbl.setX(hudX);
            this.debugStatusLbl.setY(y);
            y += lineHeight;
        }
        if (this.tipsLbl != null) {
            this.tipsLbl.setX(hudX);
            this.tipsLbl.setY(y);
            y += lineHeight * 2; // etwas Luft zu Quest
        }
        if (this.questLbl != null) {
            this.questLbl.setX(hudX);
            this.questLbl.setY(y);
            y += lineHeight;
        }
        if (this.filesProgressLbl != null) {
            this.filesProgressLbl.setX(hudX);
            this.filesProgressLbl.setY(y);
        }
    }

    /**
     * Aktualisiert die Debug-Bar oben links in Giftgrün. Sichtbar nur wenn showDebugBar = true.
     */
    private void updateDebugBar(boolean onGround, double moveSpeed, double jumpPower) {
        if (this.debugBarLbl == null) return;
        this.debugBarLbl.setVisible(showDebugBar);
        if (!showDebugBar) {
            return;
        }
        String line1 = "FPS: " + (int) screenManager.getCurrentFps()
                + " | onGround: " + onGround
                + " | moveSpeed: " + moveSpeed
                + " | jumpPower: " + jumpPower;
        String line2 = "cameraX: " + (int) cameraX
                + " | cameraY: " + (int) cameraY
                + " | Location: " + player.getLocation().toString();
        String line3 = "HP: " + (int) Math.round(player.getHealth() / player.getMaxHealth() * 100.0)
                + " | NoClip: " + player.isNoClipEnabled()
                + " | God: " + player.isGodModeEnabled();
        this.debugBarLbl.setText(line1 + "\n" + line2 + "\n" + line3);
    }

    private int countFolderBlocks() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .count();
    }

    private int countActiveFolders() {
        return (int) Game.getInstance().getCurrentLevel().getBlocks().stream()
                .filter(block -> block instanceof de.cyzetlc.hsbi.game.gui.block.impl.FolderBlock)
                .filter(de.cyzetlc.hsbi.game.gui.block.Block::isActive)
                .count();
    }

    private void updateFolderProgress() {
        if (this.totalFolderCount == 0) {
            this.filesProgressLbl.setText("Files: 0/0");
            return;
        }
        int active = countActiveFolders();
        int collected = Math.max(0, this.totalFolderCount - active);
        this.filesProgressLbl.setText("Files: " + collected + "/" + this.totalFolderCount);
    }
}
