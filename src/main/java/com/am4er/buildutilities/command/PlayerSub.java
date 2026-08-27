package com.am4er.buildutilities.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerSub implements Sub {

    @Override public final boolean playerOnly() { return true; }

    @Override
    public final void run(CommandSender sender, String label, String[] args) {
        run((Player) sender, label, args);
    }

    protected abstract void run(Player p, String label, String[] args);
}
