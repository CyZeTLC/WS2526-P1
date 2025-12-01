package de.cyzetlc.hsbi.game.world;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter @Setter
public class Location implements Serializable {
    @Serial
    private static final long serialVersionUID = 1234567L;

    private double x;
    private double y;

    public Location() {
        this.x = 0;
        this.y = 0;
    }

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Location location) {
        return Math.sqrt(Math.pow(location.getX() - this.x, 2) + Math.pow(location.getY() - this.y, 2));
    }

    @Override
    public String toString() {
        return "X: " + (int) this.getX() + ", Y: " + (int) this.getY();
    }
}
