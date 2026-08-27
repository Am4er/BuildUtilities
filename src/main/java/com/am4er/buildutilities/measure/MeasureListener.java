package com.am4er.buildutilities.measure;

import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Locale;

public final class MeasureListener implements Listener {

    private final MeasureTool tool;
    private final SessionManager sessions;

    public MeasureListener(MeasureTool tool, SessionManager sessions) {
        this.tool = tool;
        this.sessions = sessions;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !tool.is(event.getItem())) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player p = event.getPlayer();
        if (!p.hasPermission(Perms.MEASURE)) {
            return;
        }
        event.setCancelled(true);

        BuilderSession s = sessions.of(p);
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            s.mark(block.getLocation());
            Msg.good(p, "Corner pinned at " + at(block.getLocation()) + ".");
            return;
        }
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Location from = s.mark();
        if (from == null) {
            Msg.bad(p, "Left-click a block first to pin a corner.");
            return;
        }
        if (!block.getWorld().equals(from.getWorld())) {
            s.mark(null);
            Msg.bad(p, "That corner was in another world, pin a new one.");
            return;
        }
        report(p, from, block.getLocation());
    }

    private static void report(Player p, Location a, Location b) {
        int dx = Math.abs(a.getBlockX() - b.getBlockX()) + 1;
        int dy = Math.abs(a.getBlockY() - b.getBlockY()) + 1;
        int dz = Math.abs(a.getBlockZ() - b.getBlockZ()) + 1;
        long volume = (long) dx * dy * dz;

        Msg.send(p, Component.text(dx + " x " + dy + " x " + dz, Msg.BRAND)
                .append(Component.text(" wide, tall, deep", Msg.BODY)));
        Msg.hint(p, String.format(Locale.ROOT, "%,d blocks enclosed, %.2f corner to corner",
                volume, a.distance(b)));
    }

    private static String at(Location where) {
        return where.getBlockX() + ", " + where.getBlockY() + ", " + where.getBlockZ();
    }
}
