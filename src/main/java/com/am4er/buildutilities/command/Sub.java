package com.am4er.buildutilities.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Sub {

    String name();

    String permission();

    String usage();

    String blurb();

    void run(CommandSender sender, String label, String[] args);

    default List<String> aliases() { return List.of(); }

    default boolean playerOnly() { return true; }

    default List<String> complete(CommandSender sender, String[] args) { return List.of(); }
}
