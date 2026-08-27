package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.BuildUtilities;
import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.Settings;
import com.am4er.buildutilities.command.Sub;
import org.bukkit.command.CommandSender;

public final class ReloadSub implements Sub {

    private final BuildUtilities plugin;

    public ReloadSub(BuildUtilities plugin) { this.plugin = plugin; }

    @Override public String name() { return "reload"; }
    @Override public String permission() { return Perms.RELOAD; }
    @Override public String usage() { return "reload"; }
    @Override public String blurb() { return "Re-read config.yml."; }
    @Override public boolean playerOnly() { return false; }

    @Override
    public void run(CommandSender sender, String label, String[] args) {
        Settings now = plugin.reloadSettings();
        Msg.good(sender, "Config reloaded. Radius " + now.radius()
                + ", sweep every " + now.scanPeriod() + " ticks.");
    }
}
