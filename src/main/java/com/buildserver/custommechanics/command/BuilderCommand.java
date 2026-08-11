package com.buildserver.custommechanics.command;

import com.buildserver.custommechanics.CustomMechanics;
import com.buildserver.custommechanics.ghost.BarrierGhostManager;
import com.buildserver.custommechanics.tools.ToolsMenu;
import com.buildserver.custommechanics.tools.ToolsMenuListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BuilderCommand implements CommandExecutor, TabCompleter {

    private static final Component PREFIX =
            Component.text("[Builder] ", NamedTextColor.DARK_AQUA);

    private final CustomMechanics plugin;
    private final BarrierGhostManager ghostManager;

    public BuilderCommand(CustomMechanics plugin, BarrierGhostManager ghostManager) {
        this.plugin = plugin;
        this.ghostManager = ghostManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "tools", "tool", "menu" -> tools(sender);
            case "ghost", "barrier", "noclip" -> ghost(sender, args);
            case "help" -> {
                sendHelp(sender, label);
                yield true;
            }
            default -> {
                send(sender, Component.text("Unknown subcommand. Try /" + label + " help.",
                        NamedTextColor.RED));
                yield true;
            }
        };
    }

    private boolean tools(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            send(sender, Component.text("Only a player can open the tool menu.", NamedTextColor.RED));
            return true;
        }
        if (!player.hasPermission(ToolsMenuListener.USE_PERMISSION)) {
            send(player, Component.text("You do not have permission to use the builder tools.",
                    NamedTextColor.RED));
            return true;
        }
        new ToolsMenu(plugin.tools()).open(player);
        return true;
    }

    private boolean ghost(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            send(sender, Component.text("Only a player can toggle ghost mode.", NamedTextColor.RED));
            return true;
        }
        if (!player.hasPermission(BarrierGhostManager.PERMISSION)) {
            send(player, Component.text("You do not have permission to walk through barriers.",
                    NamedTextColor.RED));
            return true;
        }

        boolean nowEnabled;
        if (args.length >= 2) {
            Boolean requested = parseState(args[1]);
            if (requested == null) {
                send(player, Component.text("Use /builder ghost [on|off].", NamedTextColor.RED));
                return true;
            }
            if (requested == ghostManager.isEnabled(player)) {
                send(player, Component.text("Barrier ghost mode is already ", NamedTextColor.GRAY)
                        .append(state(requested))
                        .append(Component.text(".", NamedTextColor.GRAY)));
                return true;
            }
            if (requested) {
                ghostManager.enable(player);
            } else {
                ghostManager.disable(player);
            }
            nowEnabled = requested;
        } else {
            nowEnabled = ghostManager.toggle(player);
        }

        send(player, Component.text("Barrier ghost mode ", NamedTextColor.GRAY).append(state(nowEnabled)));
        if (nowEnabled) {
            send(player, Component.text("Barriers near you are hidden from your client only - "
                    + "walk straight through them.", NamedTextColor.DARK_GRAY));
        }
        return true;
    }

    private static @Nullable Boolean parseState(String argument) {
        return switch (argument.toLowerCase(Locale.ROOT)) {
            case "on", "true", "enable", "enabled", "yes" -> Boolean.TRUE;
            case "off", "false", "disable", "disabled", "no" -> Boolean.FALSE;
            default -> null;
        };
    }

    private static Component state(boolean enabled) {
        return enabled
                ? Component.text("enabled", NamedTextColor.GREEN)
                : Component.text("disabled", NamedTextColor.RED);
    }

    private void sendHelp(CommandSender sender, String label) {
        send(sender, Component.text("Custom mechanics for builders", NamedTextColor.AQUA));
        sender.sendMessage(helpLine(label, "tools", "Open the builder tool menu."));
        sender.sendMessage(helpLine(label, "ghost [on|off]", "Toggle walking through barrier blocks."));
    }

    private static Component helpLine(String label, String usage, String description) {
        return Component.text("  /" + label + " " + usage, NamedTextColor.YELLOW)
                .append(Component.text(" - " + description, NamedTextColor.GRAY));
    }

    private static void send(CommandSender sender, Component message) {
        sender.sendMessage(PREFIX.append(message));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            return filter(List.of("tools", "ghost", "help"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("ghost")) {
            return filter(List.of("on", "off"), args[1]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lowered = prefix.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.startsWith(lowered)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
