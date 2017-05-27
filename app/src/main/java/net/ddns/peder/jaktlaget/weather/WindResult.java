package net.ddns.peder.jaktlaget.weather;

/**
 * Created by peder on 5/26/17.
 */

public class WindResult {
    private final float bearing;
    private final float speed;

    public WindResult(float bearing, float speed) {
        this.bearing = bearing;
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public float getSpeed() {
        return speed;
    }
}


