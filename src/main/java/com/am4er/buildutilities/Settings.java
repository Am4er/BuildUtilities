package com.am4er.buildutilities;

import org.bukkit.configuration.file.FileConfiguration;

public record Settings(int radius, int scanPeriod, boolean persist) {
    private static final int MAX_RADIUS = 16;

    public static Settings read(FileConfiguration cfg) {
        return new Settings(
                Math.clamp(cfg.getInt("barrier-ghost.radius", 6), 1, MAX_RADIUS),
                Math.clamp(cfg.getInt("barrier-ghost.refresh-interval", 40), 5, 400),
                cfg.getBoolean("barrier-ghost.persist", true));
    }

    public int scanVolume() {
        int side = radius * 2 + 1;
        return side * side * side;
    }
}
