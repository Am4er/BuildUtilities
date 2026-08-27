package com.am4er.buildutilities.command;

import com.am4er.buildutilities.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BuilderCommand implements CommandExecutor, TabCompleter {

    private final List<Sub> subs;
    private final Map<String, Sub> lookup = new HashMap<>();

    public BuilderCommand(List<Sub> subs) {
        this.subs = List.copyOf(subs);
        for (Sub sub : this.subs) {
            lookup.put(sub.name(), sub);
            for (String alias : sub.aliases()) {
                lookup.put(alias, sub);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            help(sender, label);
            return true;
        }

        Sub sub = lookup.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            Msg.bad(sender, "No such subcommand. Try /" + label + " help.");
            return true;
        }
        if (!sender.hasPermission(sub.permission())) {
            Msg.bad(sender, "You do not have permission for that.");
            return true;
        }
        if (sub.playerOnly() && !(sender instanceof Player)) {
            Msg.bad(sender, "Only a player can run that.");
            return true;
        }

        sub.run(sender, label, args);
        return true;
    }

    private void help(CommandSender sender, String label) {
        Msg.send(sender, Component.text("Utilities for builders", Msg.BRAND));
        for (Sub sub : subs) {
            if (sender.hasPermission(sub.permission())) {
                sender.sendMessage(Component.text("  /" + label + " " + sub.usage(), Msg.WARN)
                        .append(Component.text("  " + sub.blurb(), Msg.FAINT)));
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>(subs.size() + 1);
            for (Sub sub : subs) {
                if (sender.hasPermission(sub.permission())) {
                    names.add(sub.name());
                }
            }
            names.add("help");
            return matching(names, args[0]);
        }

        Sub sub = lookup.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null || !sender.hasPermission(sub.permission())) {
            return List.of();
        }
        return matching(sub.complete(sender, args), args[args.length - 1]);
    }

    private static List<String> matching(List<String> options, String typed) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        List<String> hits = new ArrayList<>(options.size());
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                hits.add(option);
            }
        }
        return hits;
    }
}
