package de.cyzetlc.hsbi.game.gui.block;

public enum Material {
    AIR(""),
    DIRT("/assets/tileset/dirt_tiles.png"),
    SANDSTONE("/assets/tileset/sandstone_tiles.png"),
    FLOOR("/assets/tileset/floor_tiles.png"),
    LAVA("/assets/lavaset/1Lava32x64.png"),
    JUMP_PERK("/assets/tileset/jump_boost.png"),
    FLIPPER("/assets/tileset/flipper_item.png"),
    SPEED_PERK("/assets/tileset/speed.png"),
    ROBOT_ENEMY("/assets/tileset/roboter_enemy.png"),
    ROBOT_LASER("/assets/tileset/robot_laser.png"),
    USB_STICK("/assets/USB-Stick/USB-Stick-Raw.png"),
    FOLDER_ITEM("/assets/tileset/folder.png"),
    FINISH_FLAG("/assets/USB-Stick/PortalTutorial/Portal1-removebg-preview.png"),
    SERVER("/assets/hud/PlatformServerMehreZustände/Zugeschnitten1.png"),
    FLOATING_PLATFORM("/assets/movingplatform/1MovingPlatform32x64.png");

    public String texturePath;

    Material(String texturePath) {
        this.texturePath = texturePath;
    }
}
