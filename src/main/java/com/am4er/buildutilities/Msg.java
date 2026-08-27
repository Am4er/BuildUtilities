package com.am4er.buildutilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public final class Msg {
    public static final TextColor BRAND = TextColor.color(0x4EC9B0);
    public static final TextColor GOOD = TextColor.color(0x89D185);
    public static final TextColor BAD = TextColor.color(0xE06C61);
    public static final TextColor WARN = TextColor.color(0xE2C08D);
    public static final TextColor BODY = TextColor.color(0xCCCCCC);
    public static final TextColor FAINT = TextColor.color(0x7F8C8D);

    private static final Component PREFIX = Component.text()
            .append(Component.text("[", FAINT))
            .append(Component.text("BuildUtils", BRAND))
            .append(Component.text("] ", FAINT))
            .build();

    private Msg() {
    }

    public static void info(CommandSender to, String line) {
        to.sendMessage(PREFIX.append(Component.text(line, BODY)));
    }

    public static void good(CommandSender to, String line) {
        to.sendMessage(PREFIX.append(Component.text(line, GOOD)));
    }

    public static void bad(CommandSender to, String line) {
        to.sendMessage(PREFIX.append(Component.text(line, BAD)));
    }

    public static void warn(CommandSender to, String line) {
        to.sendMessage(PREFIX.append(Component.text(line, WARN)));
    }

    public static void hint(CommandSender to, String line) {
        to.sendMessage(PREFIX.append(Component.text(line, FAINT)));
    }

    public static void send(CommandSender to, Component body) {
        to.sendMessage(PREFIX.append(body));
    }

    public static Component label(String text, TextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    public static Component state(boolean on) {
        return Component.text(on ? "on" : "off", on ? GOOD : BAD);
    }
}
