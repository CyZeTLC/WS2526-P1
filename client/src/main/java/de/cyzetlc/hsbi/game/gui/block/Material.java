package de.cyzetlc.hsbi.game.gui.block;

public enum Material {
    AIR(""),
    DIRT("/assets/tileset/dirt_tiles.png"),
    SANDSTONE("/assets/tileset/sandstone_tiles.png"),
    LAVA("/assets/lavaset/1Lava32x64.png"),
    JUMP_PERK("/assets/tileset/jumpboost.png"),
    FLOATING_PLATFORM("/assets/movingplatform/1MovingPlatform32x64.png");

    public String texturePath;

    Material(String texturePath) {
        this.texturePath = texturePath;
    }
}
