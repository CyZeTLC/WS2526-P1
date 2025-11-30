package de.cyzetlc.hsbi.game.gui.block.impl;

import de.cyzetlc.hsbi.game.entity.EntityPlayer;
import de.cyzetlc.hsbi.game.world.Location;

/**
 * Boss-Variante des Roboters.
 * - Steht sichtbar auf Plattformoberkante (Y kommt als Plattform-Top rein)
 * - Jagt immer den Spieler (Logik in der Basisklasse)
 * - Schießt Feuerball/Laser nur, wenn der Spieler über ihm und nahe ist.
 */
public class BossRobotBlock extends RobotEnemyBlock {

    public BossRobotBlock(Location platformTopLocation, double patrolWidth, double speed) {
        super(platformTopLocation, patrolWidth, speed);
    }

    @Override
    public LaserBlock tryFire(EntityPlayer player) {
        if (dead || player == null) return null;

        // Spieler muss deutlich oberhalb sein (Sprung �ber den Boss)
        boolean playerAbove = player.getLocation().getY() + player.getHeight() < this.getLocation().getY();
        boolean closeHorizontally = Math.abs(player.getLocation().getX() - this.getLocation().getX()) < 240;
        if (!playerAbove || !closeHorizontally) {
            return null;
        }
        if (fireTimer < fireCooldown) {
            return null;
        }
        fireTimer = 0;

        int dir = player.getLocation().getX() >= this.getLocation().getX() ? 1 : -1;
        double eyeY = this.getLocation().getY() + this.getHeight() * 0.35;
        double spawnX = dir == 1 ? this.getLocation().getX() + this.getWidth() - 4 : this.getLocation().getX() - 8;

        return new LaserBlock(new Location(spawnX, eyeY), dir, 320);
    }
}
