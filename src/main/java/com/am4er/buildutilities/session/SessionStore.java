package com.am4er.buildutilities.session;

import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

final class SessionStore {

    private static final String FILE = "builders.yml";

    private final Plugin plugin;
    private final AtomicLong version = new AtomicLong();
    private final Object writeLock = new Object();

    SessionStore(Plugin plugin) {
        this.plugin = plugin;
    }

    Map<UUID, BuilderSession> load() {
        File file = new File(plugin.getDataFolder(), FILE);
        if (!file.isFile()) {
            return Map.of();
        }

        ConfigurationSection players = YamlConfiguration.loadConfiguration(file)
                .getConfigurationSection("players");
        if (players == null) {
            return Map.of();
        }

        Map<UUID, BuilderSession> out = new LinkedHashMap<>();
        for (String raw : players.getKeys(false)) {
            UUID id;
            try {
                id = UUID.fromString(raw);
            } catch (IllegalArgumentException unreadable) {
                plugin.getLogger().warning("Skipping unreadable UUID in " + FILE + ": " + raw);
                continue;
            }

            ConfigurationSection row = players.getConfigurationSection(raw);
            if (row == null) {
                continue;
            }

            BuilderSession s = new BuilderSession(row.getString("name", "unknown"));
            s.ghost(row.getBoolean("ghost", false));
            s.hiding(row.getBoolean("hide", false));
            s.bright(row.getBoolean("bright", false));
            s.speed(row.getInt("speed", BuilderSession.NORMAL_SPEED));
            s.time(row.getLong("time", BuilderSession.WORLD_TIME));
            s.weather(readWeather(row.getString("weather")));
            out.put(id, s);
        }
        return out;
    }

    void save(Map<UUID, BuilderSession> live) {
        Map<UUID, Snapshot> snapshot = snapshot(live);
        long stamp = version.incrementAndGet();
        if (!plugin.isEnabled()) {
            write(snapshot, stamp);
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> write(snapshot, stamp));
    }

    void saveBlocking(Map<UUID, BuilderSession> live) {
        write(snapshot(live), version.incrementAndGet());
    }

    private static Map<UUID, Snapshot> snapshot(Map<UUID, BuilderSession> live) {
        Map<UUID, Snapshot> copy = new LinkedHashMap<>();
        live.forEach((id, s) -> {
            if (!s.isDefault()) {
                copy.put(id, Snapshot.of(s));
            }
        });
        return copy;
    }

    private void write(Map<UUID, Snapshot> snapshot, long stamp) {
        synchronized (writeLock) {
            if (stamp != version.get()) {
                return;
            }

            YamlConfiguration out = new YamlConfiguration();
            out.options().setHeader(List.of(
                    "Per builder settings for BuildUtilities.",
                    "Rewritten whenever somebody changes one and read back at startup.",
                    "Anyone sitting on defaults is left out entirely.",
                    "Edit this with the server stopped, a running server will overwrite it."));

            snapshot.forEach((id, row) -> {
                String at = "players." + id + ".";
                out.set(at + "name", row.name());
                if (row.ghost()) {
                    out.set(at + "ghost", true);
                }
                if (row.hiding()) {
                    out.set(at + "hide", true);
                }
                if (row.bright()) {
                    out.set(at + "bright", true);
                }
                if (row.speed() != BuilderSession.NORMAL_SPEED) {
                    out.set(at + "speed", row.speed());
                }
                if (row.time() != BuilderSession.WORLD_TIME) {
                    out.set(at + "time", row.time());
                }
                if (row.weather() != null) {
                    out.set(at + "weather", row.weather().name().toLowerCase(Locale.ROOT));
                }
            });

            File folder = plugin.getDataFolder();
            try {
                if (!folder.isDirectory() && !folder.mkdirs()) {
                    throw new IOException("could not create " + folder);
                }
                out.save(new File(folder, FILE));
            } catch (IOException failure) {
                plugin.getLogger().log(Level.WARNING, "Could not save " + FILE, failure);
            }
        }
    }

    private static WeatherType readWeather(String raw) {
        if (raw == null) {
            return null;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "clear" -> WeatherType.CLEAR;
            case "downfall", "rain" -> WeatherType.DOWNFALL;
            default -> null;
        };
    }

    private record Snapshot(String name, boolean ghost, boolean hiding, boolean bright,
                            int speed, long time, WeatherType weather) {

        static Snapshot of(BuilderSession s) {
            return new Snapshot(s.name(), s.ghost(), s.hiding(), s.bright(),
                    s.speed(), s.time(), s.weather());
        }
    }
}
