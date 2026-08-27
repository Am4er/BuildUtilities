package com.am4er.buildutilities.session;

import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.jetbrains.annotations.Nullable;

public final class BuilderSession {

    public static final long WORLD_TIME = Long.MIN_VALUE;
    public static final int NORMAL_SPEED = 1;
    public static final int MAX_SPEED = 10;

    private String name;
    private boolean ghost;
    private long time = WORLD_TIME;
    private WeatherType weather;
    private boolean hiding;
    private boolean bright;
    private int speed = NORMAL_SPEED;
    private Location mark;

    BuilderSession(String name) { this.name = name; }

    public String name() { return name; }
    void name(String name) { this.name = name; }

    public boolean ghost() { return ghost; }
    public void ghost(boolean on) { this.ghost = on; }

    public long time() { return time; }
    public void time(long ticks) { this.time = ticks; }
    public boolean hasTime() { return time != WORLD_TIME; }

    public @Nullable WeatherType weather() { return weather; }
    public void weather(@Nullable WeatherType type) { this.weather = type; }

    public boolean hiding() { return hiding; }
    public void hiding(boolean on) { this.hiding = on; }

    public boolean bright() { return bright; }
    public void bright(boolean on) { this.bright = on; }

    public int speed() { return speed; }
    public void speed(int level) { this.speed = Math.clamp(level, NORMAL_SPEED, MAX_SPEED); }

    public @Nullable Location mark() { return mark; }
    public void mark(@Nullable Location where) { this.mark = where; }

    public boolean isDefault() {
        return !ghost
                && time == WORLD_TIME
                && weather == null
                && !hiding
                && !bright
                && speed == NORMAL_SPEED;
    }
}
