package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.PlayerSub;
import com.am4er.buildutilities.measure.MeasureTool;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class MeasureSub extends PlayerSub {

    private final MeasureTool tool;
    private final SessionManager sessions;

    public MeasureSub(MeasureTool tool, SessionManager sessions) {
        this.tool = tool;
        this.sessions = sessions;
    }

    @Override public String name() { return "measure"; }
    @Override public String permission() { return Perms.MEASURE; }
    @Override public String usage() { return "measure"; }
    @Override public String blurb() { return "Get the measuring tape."; }

    @Override
    protected void run(Player p, String label, String[] args) {
        sessions.of(p).mark(null);

        Map<Integer, ItemStack> spare = p.getInventory().addItem(tool.item());
        if (!spare.isEmpty()) {
            spare.values().forEach(over -> p.getWorld().dropItemNaturally(p.getLocation(), over));
            Msg.warn(p, "Inventory full, the tape is at your feet.");
        }
        Msg.hint(p, "Left-click a block to pin a corner, right-click another to measure.");
    }
}
