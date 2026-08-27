package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.PlayerSub;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TimeSub extends PlayerSub {

    private static final long DAY_LENGTH = 24000L;
    private static final Map<String, Long> PRESETS = new LinkedHashMap<>();

    static {
        PRESETS.put("sunrise", 23000L);
        PRESETS.put("day", 1000L);
        PRESETS.put("noon", 6000L);
        PRESETS.put("sunset", 12000L);
        PRESETS.put("night", 14000L);
        PRESETS.put("midnight", 18000L);
    }

    private final SessionManager sessions;

    public TimeSub(SessionManager sessions) { this.sessions = sessions; }

    @Override public String name() { return "time"; }
    @Override public String permission() { return Perms.TIME; }
    @Override public String usage() { return "time <day|noon|night|ticks|reset>"; }
    @Override public String blurb() { return "Freeze the sky at a fixed time, for you only."; }

    @Override
    protected void run(Player p, String label, String[] args) {
        BuilderSession s = sessions.of(p);

        if (args.length < 2) {
            if (s.hasTime()) {
                Msg.info(p, "Your sky is locked at " + s.time() + ".");
            } else {
                Msg.info(p, "Your sky follows the world.");
            }
            Msg.hint(p, "Use /" + label + " " + usage() + ".");
            return;
        }

        String word = args[1].toLowerCase(Locale.ROOT);
        if (word.equals("reset") || word.equals("world")) {
            s.time(BuilderSession.WORLD_TIME);
            sessions.applyTime(p, s);
            sessions.save();
            Msg.good(p, "Your sky follows the world again.");
            return;
        }

        Long ticks = PRESETS.get(word);
        if (ticks == null) {
            try {
                ticks = Long.parseLong(word);
            } catch (NumberFormatException notANumber) {
                Msg.bad(p, "Use a preset such as noon, or a tick count from 0 to " + DAY_LENGTH + ".");
                return;
            }
            if (ticks < 0 || ticks >= DAY_LENGTH) {
                Msg.bad(p, "Ticks run from 0 to " + (DAY_LENGTH - 1) + ".");
                return;
            }
        }

        s.time(ticks);
        sessions.applyTime(p, s);
        sessions.save();
        Msg.good(p, "Your sky is locked at " + ticks + ".");
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return List.of();
        }
        List<String> options = new ArrayList<>(PRESETS.keySet());
        options.add("reset");
        return options;
    }
}
