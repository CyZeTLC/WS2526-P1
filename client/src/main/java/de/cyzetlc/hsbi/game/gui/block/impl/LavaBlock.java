package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.Game;
import de.cyzetlc.hsbi.game.entity.Player;
import de.cyzetlc.hsbi.game.gui.block.AnimatedBlock;
import de.cyzetlc.hsbi.game.gui.block.Material;
import de.cyzetlc.hsbi.game.gui.screens.GameScreen;
import de.cyzetlc.hsbi.game.gui.screens.MainMenuScreen;
import de.cyzetlc.hsbi.game.world.Location;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class LavaBlock extends AnimatedBlock {
    private final List<ImageView> tiles = new ArrayList<>();
    private Image lastFrame;

    public LavaBlock(Location location) {
        super(location, new String[] {
                "/assets/lavaset/lava_1.png",
                "/assets/lavaset/lava_2.png",
                "/assets/lavaset/lava_3.png"
        });
        this.setMaterial(Material.LAVA);
    }

    @Override
    public void draw(Pane pane) {
        super.draw(pane);
        // hide stretched base sprite; render tiled quads instead
        this.getSprite().setVisible(false);
        this.rebuildTiles(pane, this.getSprite().getImage());
    }

    @Override
    public void onCollide(Player player) {
        float newHealth = Math.max(0, player.getHealth() - 0.25f); // 25% Schaden pro Tick
        player.setHealth(newHealth);
        if (newHealth <= 0 && Game.getInstance() != null) {
            Game.getInstance().getScreenManager().showScreen(new MainMenuScreen(Game.getInstance().getScreenManager()));
        }
    }

    @Override
    public void update() {
        // keep animation timing from AnimatedBlock
        super.update();

        Image currentFrame = this.getSprite().getImage();
        double camX = 0;
        double camY = 0;
        if (Game.getInstance() != null && Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen) {
            GameScreen gs = (GameScreen) Game.getInstance().getScreenManager().getCurrentScreen();
            camX = gs.getCameraX();
            camY = gs.getCameraY();
        }

        // swap frame on tiles if animation advanced
        if (currentFrame != null && currentFrame != lastFrame) {
            for (ImageView tile : tiles) {
                tile.setImage(currentFrame);
            }
            lastFrame = currentFrame;
        }

        double[] tileSize = this.getTileSize(currentFrame);
        double tileWidth = tileSize[0];
        double tileHeight = tileSize[1];

        int cols = (int) Math.ceil(this.getWidth() / tileWidth);
        int rows = (int) Math.ceil(this.getHeight() / tileHeight);

        int index = 0;
        for (int i = 0; i < cols; i++) {
            double drawW = (i == cols - 1) ? Math.min(tileWidth, this.getWidth() - (i * tileWidth)) : tileWidth;
            for (int j = 0; j < rows; j++) {
                if (index >= tiles.size()) {
                    return; // safety
                }
                double drawH = (j == rows - 1) ? Math.min(tileHeight, this.getHeight() - (j * tileHeight)) : tileHeight;
                double viewportW = currentFrame != null && tileWidth > 0 ? currentFrame.getWidth() * (drawW / tileWidth) : drawW;
                double viewportH = currentFrame != null && tileHeight > 0 ? currentFrame.getHeight() * (drawH / tileHeight) : drawH;
                ImageView tile = tiles.get(index++);
                tile.setViewport(new Rectangle2D(0, 0, viewportW, viewportH));
                tile.setFitWidth(drawW);
                tile.setFitHeight(drawH);
                tile.setX(this.getLocation().getX() + i * tileWidth - camX);
                tile.setY(this.getLocation().getY() + j * tileHeight - camY);
            }
        }
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (!active && this.getPane() != null) {
            this.getPane().getChildren().removeAll(tiles);
        }
    }

    private void rebuildTiles(Pane pane, Image frame) {
        if (pane == null) {
            return;
        }
        pane.getChildren().removeAll(tiles);
        tiles.clear();
        if (frame == null) {
            return;
        }

        double[] tileSize = this.getTileSize(frame);
        double tileWidth = tileSize[0];
        double tileHeight = tileSize[1];

        int cols = (int) Math.ceil(this.getWidth() / tileWidth);
        int rows = (int) Math.ceil(this.getHeight() / tileHeight);

        double camX = 0;
        double camY = 0;
        if (Game.getInstance() != null && Game.getInstance().getScreenManager().getCurrentScreen() instanceof GameScreen) {
            GameScreen gs = (GameScreen) Game.getInstance().getScreenManager().getCurrentScreen();
            camX = gs.getCameraX();
            camY = gs.getCameraY();
        }

        for (int i = 0; i < cols; i++) {
            double drawW = (i == cols - 1) ? Math.min(tileWidth, this.getWidth() - (i * tileWidth)) : tileWidth;
            for (int j = 0; j < rows; j++) {
                double drawH = (j == rows - 1) ? Math.min(tileHeight, this.getHeight() - (j * tileHeight)) : tileHeight;
                double viewportW = frame.getWidth() * (drawW / tileWidth);
                double viewportH = frame.getHeight() * (drawH / tileHeight);
                ImageView tile = new ImageView(frame);
                tile.setSmooth(false);
                tile.setViewport(new Rectangle2D(0, 0, viewportW, viewportH));
                tile.setFitWidth(drawW);
                tile.setFitHeight(drawH);
                tile.setX(this.getLocation().getX() + i * tileWidth - camX);
                tile.setY(this.getLocation().getY() + j * tileHeight - camY);
                pane.getChildren().add(tile);
                tiles.add(tile);
            }
        }
        this.lastFrame = frame;
    }

    private double[] getTileSize(Image frame) {
        double baseWidth = 64;
        double baseHeight = 64;
        if (frame != null && frame.getWidth() > 0 && frame.getHeight() > 0) {
            baseHeight = frame.getHeight() * (baseWidth / frame.getWidth());
            baseHeight = Math.max(32, Math.min(96, baseHeight));
        }
        return new double[] { baseWidth, baseHeight };
    }
}
