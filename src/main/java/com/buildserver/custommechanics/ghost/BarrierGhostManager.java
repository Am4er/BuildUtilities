package com.buildserver.custommechanics.ghost;

import com.buildserver.custommechanics.CustomMechanics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class BarrierGhostManager {

    public static final String PERMISSION = "custommechanics.ghost";
    private static final String STATE_FILE = "ghost-players.yml";

    private record BlockKey(int x, int y, int z) {
    }

    private final CustomMechanics plugin;

    private final Set<UUID> enabled = new HashSet<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Map<UUID, Set<BlockKey>> hidden = new HashMap<>();
    private final Map<UUID, UUID> hiddenWorlds = new HashMap<>();

    private final BlockData air = Material.AIR.createBlockData();

    private int radius;
    private int refreshInterval;
    private boolean persist;
    private BukkitTask refreshTask;

    public BarrierGhostManager(CustomMechanics plugin) {
        this.plugin = plugin;
    }

    public void start() {
        radius = Math.clamp(plugin.getConfig().getInt("barrier-ghost.radius", 6), 1, 32);
        refreshInterval = Math.max(1, plugin.getConfig().getInt("barrier-ghost.refresh-interval", 20));
        persist = plugin.getConfig().getBoolean("barrier-ghost.persist", true);

        load();

        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, refreshInterval, refreshInterval);

        for (Player player : Bukkit.getOnlinePlayers()) {
            handleJoin(player);
        }
    }

    public void shutdown() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isEnabled(player)) {
                restore(player);
            }
        }
        save();
    }

    public boolean isEnabled(Player player) {
        return enabled.contains(player.getUniqueId());
    }

    public void enable(Player player) {
        enabled.add(player.getUniqueId());
        names.put(player.getUniqueId(), player.getName());
        refresh(player, true);
        save();
    }

    public void disable(Player player) {
        enabled.remove(player.getUniqueId());
        names.remove(player.getUniqueId());
        restore(player);
        save();
    }

    public boolean toggle(Player player) {
        if (isEnabled(player)) {
            disable(player);
            return false;
        }
        enable(player);
        return true;
    }

    public void refreshIfEnabled(Player player, boolean force) {
        if (isEnabled(player)) {
            refresh(player, force);
        }
    }

    public void refreshNear(Location location) {
        if (enabled.isEmpty()) {
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            double reach = (radius + 2) * (radius + 2);
            for (UUID id : List.copyOf(enabled)) {
                Player player = Bukkit.getPlayer(id);
                if (player == null || !player.getWorld().equals(location.getWorld())) {
                    continue;
                }
                if (player.getLocation().distanceSquared(location) <= reach) {
                    refresh(player, true);
                }
            }
        });
    }

    public void handleJoin(Player player) {
        if (!isEnabled(player)) {
            return;
        }
        if (!player.hasPermission(PERMISSION)) {
            disable(player);
            return;
        }
        if (!player.getName().equals(names.put(player.getUniqueId(), player.getName()))) {
            save();
        }

        hidden.remove(player.getUniqueId());
        hiddenWorlds.remove(player.getUniqueId());
        refresh(player, true);
    }

    public void handleQuit(Player player) {
        UUID id = player.getUniqueId();
        hidden.remove(id);
        hiddenWorlds.remove(id);
        if (!persist) {
            enabled.remove(id);
            names.remove(id);
        }
    }

    public void handleWorldChange(Player player) {
        UUID id = player.getUniqueId();
        hidden.remove(id);
        hiddenWorlds.remove(id);
        refreshIfEnabled(player, true);
    }

    private void tick() {
        for (UUID id : List.copyOf(enabled)) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) {
                continue;
            }
            if (!player.hasPermission(PERMISSION)) {
                disable(player);
                continue;
            }
            refresh(player, true);
        }
    }

    private void refresh(Player player, boolean force) {
        UUID id = player.getUniqueId();
        World world = player.getWorld();

        UUID previousWorld = hiddenWorlds.get(id);
        if (previousWorld != null && !previousWorld.equals(world.getUID())) {
            hidden.remove(id);
        }
        Set<BlockKey> previous = hidden.getOrDefault(id, Set.of());

        Location origin = player.getLocation();
        int centreX = origin.getBlockX();
        int centreY = origin.getBlockY();
        int centreZ = origin.getBlockZ();
        int minY = Math.max(world.getMinHeight(), centreY - radius);
        int maxY = Math.min(world.getMaxHeight() - 1, centreY + radius);

        Set<BlockKey> current = new LinkedHashSet<>();
        for (int x = centreX - radius; x <= centreX + radius; x++) {
            for (int z = centreZ - radius; z <= centreZ + radius; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    if (world.getBlockAt(x, y, z).getType() != Material.BARRIER) {
                        continue;
                    }
                    BlockKey key = new BlockKey(x, y, z);
                    current.add(key);
                    if (force || !previous.contains(key)) {
                        player.sendBlockChange(new Location(world, x, y, z), air);
                    }
                }
            }
        }

        for (BlockKey key : previous) {
            if (!current.contains(key)) {
                sendActualBlock(player, world, key);
            }
        }

        hidden.put(id, current);
        hiddenWorlds.put(id, world.getUID());
    }

    private void restore(Player player) {
        UUID id = player.getUniqueId();
        Set<BlockKey> keys = hidden.remove(id);
        UUID worldId = hiddenWorlds.remove(id);
        if (keys == null || worldId == null || !player.isOnline()) {
            return;
        }
        World world = player.getWorld();
        if (!world.getUID().equals(worldId)) {
            return;
        }
        for (BlockKey key : keys) {
            sendActualBlock(player, world, key);
        }
    }

    private void sendActualBlock(Player player, World world, BlockKey key) {
        if (!world.isChunkLoaded(key.x() >> 4, key.z() >> 4)) {
            return;
        }
        Location location = new Location(world, key.x(), key.y(), key.z());
        player.sendBlockChange(location, world.getBlockAt(key.x(), key.y(), key.z()).getBlockData());
    }

    private void load() {
        if (!persist) {
            return;
        }
        File file = new File(plugin.getDataFolder(), STATE_FILE);
        if (!file.isFile()) {
            return;
        }
        YamlConfiguration state = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection players = state.getConfigurationSection("players");
        if (players == null) {
            return;
        }
        for (String raw : players.getKeys(false)) {
            UUID id;
            try {
                id = UUID.fromString(raw);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Ignoring malformed UUID in " + STATE_FILE + ": " + raw);
                continue;
            }
            if (!players.getBoolean(raw + ".ghost", true)) {
                continue;
            }
            enabled.add(id);
            String name = players.getString(raw + ".name");
            if (name != null) {
                names.put(id, name);
            }
        }
        if (!enabled.isEmpty()) {
            plugin.getLogger().info("Restored barrier ghost mode for " + enabled.size() + " builder(s): "
                    + String.join(", ", describe()));
        }
    }

    private void save() {
        if (!persist) {
            return;
        }
        YamlConfiguration state = new YamlConfiguration();
        state.options().setHeader(List.of(
                "Builders who have barrier ghost mode switched on.",
                "Written by CustomMechanics whenever someone toggles it, and read back at startup.",
                "Names are only here to make the file readable - the UUID is what counts.",
                "Edit this while the server is stopped; a running server will overwrite it."));
        for (UUID id : enabled) {
            state.set("players." + id + ".name", names.getOrDefault(id, "unknown"));
            state.set("players." + id + ".ghost", true);
        }
        File file = new File(plugin.getDataFolder(), STATE_FILE);
        try {
            if (!plugin.getDataFolder().isDirectory() && !plugin.getDataFolder().mkdirs()) {
                throw new IOException("could not create " + plugin.getDataFolder());
            }
            state.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not save " + STATE_FILE, exception);
        }
    }

    private List<String> describe() {
        List<String> described = new ArrayList<>(enabled.size());
        for (UUID id : enabled) {
            described.add(names.getOrDefault(id, id.toString()));
        }
        return described;
    }
}
