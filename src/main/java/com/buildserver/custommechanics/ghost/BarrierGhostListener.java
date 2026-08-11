package com.buildserver.custommechanics.ghost;

import com.buildserver.custommechanics.CustomMechanics;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class BarrierGhostListener implements Listener {

    private final CustomMechanics plugin;
    private final BarrierGhostManager manager;

    public BarrierGhostListener(CustomMechanics plugin, BarrierGhostManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock()) {
            manager.refreshIfEnabled(event.getPlayer(), false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        nextTick(event.getPlayer(), player -> manager.refreshIfEnabled(player, true));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        manager.handleWorldChange(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        nextTick(event.getPlayer(), player -> manager.refreshIfEnabled(player, true));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFailMove(PlayerFailMoveEvent event) {
        if (!manager.isEnabled(event.getPlayer())) {
            return;
        }
        PlayerFailMoveEvent.FailReason reason = event.getFailReason();
        if (reason == PlayerFailMoveEvent.FailReason.CLIPPED_INTO_BLOCK
                || reason == PlayerFailMoveEvent.FailReason.MOVED_WRONGLY) {
            event.setAllowed(true);
            event.setLogWarning(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        manager.handleJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        manager.handleQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.BARRIER) {
            manager.refreshNear(event.getBlockPlaced().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BARRIER) {
            manager.refreshNear(event.getBlock().getLocation());
        }
    }

    private void nextTick(Player player, java.util.function.Consumer<Player> action) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                action.accept(player);
            }
        });
    }
}
