package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.PlayerSub;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public final class WeatherSub extends PlayerSub {

    private static final List<String> OPTIONS = List.of("clear", "rain", "reset");

    private final SessionManager sessions;

    public WeatherSub(SessionManager sessions) { this.sessions = sessions; }

    @Override public String name() { return "weather"; }
    @Override public String permission() { return Perms.WEATHER; }
    @Override public String usage() { return "weather <clear|rain|reset>"; }
    @Override public String blurb() { return "Pick your own weather, for you only."; }

    @Override
    protected void run(Player p, String label, String[] args) {
        BuilderSession s = sessions.of(p);

        if (args.length < 2) {
            WeatherType current = s.weather();
            Msg.info(p, current == null
                    ? "Your weather follows the world."
                    : "Your weather is locked to " + name(current) + ".");
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "clear", "sun", "sunny" -> set(p, s, WeatherType.CLEAR);
            case "rain", "downfall", "storm" -> set(p, s, WeatherType.DOWNFALL);
            case "reset", "world" -> set(p, s, null);
            default -> Msg.bad(p, "Use /" + label + " weather <clear|rain|reset>.");
        }
    }

    private void set(Player p, BuilderSession s, WeatherType type) {
        s.weather(type);
        sessions.applyWeather(p, s);
        sessions.save();
        Msg.good(p, type == null
                ? "Your weather follows the world again."
                : "Your weather is locked to " + name(type) + ".");
    }

    private static String name(WeatherType type) {
        return type == WeatherType.CLEAR ? "clear" : "rain";
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 2 ? OPTIONS : List.of();
    }
}
