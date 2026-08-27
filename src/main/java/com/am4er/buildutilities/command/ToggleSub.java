package com.am4er.buildutilities.command;

import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public abstract class ToggleSub extends PlayerSub {

    private static final List<String> STATES = List.of("on", "off");

    protected final SessionManager sessions;

    protected ToggleSub(SessionManager sessions) { this.sessions = sessions; }

    protected abstract boolean read(BuilderSession s);

    protected abstract void write(Player p, BuilderSession s, boolean on);

    protected abstract String what();

    protected @Nullable String hint() { return null; }

    @Override public String usage() { return name() + " [on|off]"; }

    @Override
    protected void run(Player p, String label, String[] args) {
        BuilderSession s = sessions.of(p);

        boolean on;
        if (args.length >= 2) {
            Boolean wanted = parse(args[1]);
            if (wanted == null) {
                Msg.bad(p, "Use /" + label + " " + name() + " [on|off].");
                return;
            }
            if (wanted == read(s)) {
                Msg.send(p, Component.text(what() + " is already ", Msg.BODY).append(Msg.state(wanted)));
                return;
            }
            on = wanted;
        } else {
            on = !read(s);
        }

        write(p, s, on);
        sessions.save();

        Msg.send(p, Component.text(what() + " ", Msg.BODY).append(Msg.state(on)));
        String hint = hint();
        if (on && hint != null) {
            Msg.hint(p, hint);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return args.length == 2 ? STATES : List.of();
    }

    protected static @Nullable Boolean parse(String word) {
        return switch (word.toLowerCase(Locale.ROOT)) {
            case "on", "true", "enable", "enabled", "yes" -> Boolean.TRUE;
            case "off", "false", "disable", "disabled", "no" -> Boolean.FALSE;
            default -> null;
        };
    }
}
