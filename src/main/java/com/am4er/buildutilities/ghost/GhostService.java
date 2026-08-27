package com.am4er.buildutilities.ghost;

import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.Settings;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import io.papermc.paper.math.Position;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GhostService {

    private enum Pass {
        MOVED,
        SWEEP,
        RELOADED
    }

    private final Plugin plugin;
    private final SessionManager sessions;
    private final Map<UUID, GhostMask> masks = new HashMap<>();
    private final BlockData air = Material.AIR.createBlockData();

    private Settings settings;
    private BukkitTask sweepTask;

    public GhostService(Plugin plugin, SessionManager sessions) {
        this.plugin = plugin;
        this.sessions = sessions;
    }

    public void start(Settings config) {
        settings = config;
        schedule();
    }

    public void reload(Settings config) {
        settings = config;
        schedule();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isOn(p)) {
                apply(p, Pass.SWEEP);
            }
        }
    }

    public void shutdown() {
        if (sweepTask != null) {
            sweepTask.cancel();
            sweepTask = null;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isOn(p)) {
                restore(p);
            }
        }
    }

    public boolean isOn(Player p) {
        BuilderSession s = sessions.peek(p);
        return s != null && s.ghost();
    }

    public void enable(Player p) {
        sessions.of(p).ghost(true);
        sessions.save();
        apply(p, Pass.RELOADED);
    }

    public void disable(Player p) {
        sessions.of(p).ghost(false);
        sessions.save();
        restore(p);
    }

    public void moved(Player p) {
        if (isOn(p)) {
            apply(p, Pass.MOVED);
        }
    }

    public void viewReloaded(Player p) {
        if (isOn(p)) {
            apply(p, Pass.RELOADED);
        }
    }

    public void onJoin(Player p) {
        if (isOn(p)) {
            apply(p, Pass.RELOADED);
        }
    }

    public void onQuit(Player p) {
        masks.remove(p.getUniqueId());
    }

    public void nudgeAround(Location where) {
        World world = where.getWorld();
        int reach = settings.radius() + 2;
        long reachSquared = (long) reach * reach;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!isOn(p) || !p.getWorld().equals(world)) {
                    continue;
                }
                if (p.getLocation().distanceSquared(where) <= reachSquared) {
                    apply(p, Pass.SWEEP);
                }
            }
        });
    }

    private void schedule() {
        if (sweepTask != null) {
            sweepTask.cancel();
        }
        int period = settings.scanPeriod();
        sweepTask = Bukkit.getScheduler().runTaskTimer(plugin, this::sweep, period, period);
    }

    private void sweep() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isOn(p)) {
                continue;
            }
            if (!p.hasPermission(Perms.GHOST)) {
                disable(p);
                continue;
            }
            apply(p, Pass.SWEEP);
        }
    }

    private void apply(Player p, Pass pass) {
        GhostMask mask = masks.computeIfAbsent(p.getUniqueId(), id -> new GhostMask());
        World world = p.getWorld();
        UUID worldId = world.getUID();

        if (mask.world() != null && !worldId.equals(mask.world())) {
            mask.forget();
        }
        if (pass == Pass.RELOADED) {
            mask.forget();
        }

        Location at = p.getLocation();
        int cx = at.getBlockX();
        int cy = at.getBlockY();
        int cz = at.getBlockZ();
        long centre = GhostMask.key(cx, cy, cz);

        if (pass == Pass.MOVED && mask.settled(worldId, centre)) {
            return;
        }

        int r = settings.radius();
        int minY = Math.max(world.getMinHeight(), cy - r);
        int maxY = Math.min(world.getMaxHeight() - 1, cy + r);

        LongOpenHashSet found = new LongOpenHashSet();
        Map<Position, BlockData> changes = new HashMap<>();

        for (int x = cx - r; x <= cx + r; x++) {
            for (int z = cz - r; z <= cz + r; z++) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    if (world.getBlockAt(x, y, z).getType() != Material.BARRIER) {
                        continue;
                    }
                    long key = GhostMask.key(x, y, z);
                    found.add(key);
                    if (!mask.contains(key)) {
                        changes.put(Position.block(x, y, z), air);
                    }
                }
            }
        }

        collectStale(mask, found, world, changes);

        if (!changes.isEmpty()) {
            p.sendMultiBlockChange(changes);
        }
        mask.adopt(worldId, centre, found);
    }

    private void collectStale(GhostMask mask, LongOpenHashSet found, World world,
                              Map<Position, BlockData> changes) {
        LongIterator keys = mask.hidden().iterator();
        while (keys.hasNext()) {
            long key = keys.nextLong();
            if (found.contains(key)) {
                continue;
            }
            int x = GhostMask.keyX(key);
            int y = GhostMask.keyY(key);
            int z = GhostMask.keyZ(key);
            if (world.isChunkLoaded(x >> 4, z >> 4)) {
                changes.put(Position.block(x, y, z), world.getBlockAt(x, y, z).getBlockData());
            }
        }
    }

    private void restore(Player p) {
        GhostMask mask = masks.remove(p.getUniqueId());
        if (mask == null || mask.isEmpty() || !p.isOnline()) {
            return;
        }
        World world = p.getWorld();
        if (!world.getUID().equals(mask.world())) {
            return;
        }
        Map<Position, BlockData> changes = new HashMap<>();
        collectStale(mask, new LongOpenHashSet(), world, changes);
        if (!changes.isEmpty()) {
            p.sendMultiBlockChange(changes);
        }
    }
}
