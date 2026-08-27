package com.am4er.buildutilities.ghost;

import io.papermc.paper.event.player.PlayerFailMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public final class GhostListener implements Listener {

    private final Plugin plugin;
    private final GhostService ghosts;

    public GhostListener(Plugin plugin, GhostService ghosts) {
        this.plugin = plugin;
        this.ghosts = ghosts;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock()) {
            ghosts.moved(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        afterTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        afterTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        ghosts.viewReloaded(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFailMove(PlayerFailMoveEvent event) {
        if (!ghosts.isOn(event.getPlayer())) {
            return;
        }
        PlayerFailMoveEvent.FailReason reason = event.getFailReason();
        if (reason == PlayerFailMoveEvent.FailReason.CLIPPED_INTO_BLOCK
                || reason == PlayerFailMoveEvent.FailReason.MOVED_WRONGLY) {
            event.setAllowed(true);
            event.setLogWarning(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.BARRIER) {
            ghosts.nudgeAround(event.getBlockPlaced().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BARRIER) {
            ghosts.nudgeAround(event.getBlock().getLocation());
        }
    }

    private void afterTick(Player p) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (p.isOnline()) {
                ghosts.viewReloaded(p);
            }
        });
    }
}
