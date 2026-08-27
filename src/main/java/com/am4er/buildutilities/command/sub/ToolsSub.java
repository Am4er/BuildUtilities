package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.BuildUtilities;
import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.PlayerSub;
import com.am4er.buildutilities.tools.ToolsMenu;
import org.bukkit.entity.Player;

import java.util.List;

public final class ToolsSub extends PlayerSub {

    private final BuildUtilities plugin;

    public ToolsSub(BuildUtilities plugin) { this.plugin = plugin; }

    @Override public String name() { return "tools"; }
    @Override public List<String> aliases() { return List.of("tool", "menu"); }
    @Override public String permission() { return Perms.TOOLS; }
    @Override public String usage() { return "tools"; }
    @Override public String blurb() { return "Open the builder tool menu."; }

    @Override
    protected void run(Player p, String label, String[] args) {
        if (plugin.tools().isEmpty()) {
            Msg.bad(p, "No builder tools loaded on this server version.");
            return;
        }
        new ToolsMenu(plugin.tools()).open(p);
    }
}
