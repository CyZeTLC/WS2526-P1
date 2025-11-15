package de.cyzetlc.hsbi.game.gui.block;

public enum Material {
    AIR(""),
    DIRT("/assets/tileset/dirt_tiles.png"),
    LAVA("/assets/tileset/LAVA_tiles.png"),
    JUMP_PERK("/assets/tileset/jumpboost.png"),
    FLOATING_PLATFORM("/assets/movingplatform/1MovingPlatform32x64.png");

    String texturePath;

    Material(String texturePath) {
        this.texturePath = texturePath;
    }
}
