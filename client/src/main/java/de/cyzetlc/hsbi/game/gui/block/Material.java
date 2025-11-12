package de.cyzetlc.hsbi.game.gui.block;

public enum Material {
    AIR(""),
    DIRT("assets/tileset/dirt_tiles.png"),
    LAVA("assets/tileset/lava_tiles.png");;

    String texturePath;

    Material(String texturePath) {
        this.texturePath = texturePath;
    }
}
