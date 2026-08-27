package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.Msg;
import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.PlayerSub;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SpeedSub extends PlayerSub {

    private final SessionManager sessions;

    public SpeedSub(SessionManager sessions) { this.sessions = sessions; }

    @Override public String name() { return "speed"; }
    @Override public List<String> aliases() { return List.of("fly"); }
    @Override public String permission() { return Perms.SPEED; }
    @Override public String usage() { return "speed <1-" + BuilderSession.MAX_SPEED + "|reset>"; }
    @Override public String blurb() { return "Set how fast you fly."; }

    @Override
    protected void run(Player p, String label, String[] args) {
        BuilderSession s = sessions.of(p);

        if (args.length < 2) {
            Msg.info(p, "Your fly speed is " + s.speed() + " of " + BuilderSession.MAX_SPEED + ".");
            return;
        }

        String word = args[1].toLowerCase(Locale.ROOT);
        int level;
        if (word.equals("reset") || word.equals("normal")) {
            level = BuilderSession.NORMAL_SPEED;
        } else {
            try {
                level = Integer.parseInt(word);
            } catch (NumberFormatException notANumber) {
                Msg.bad(p, "Pick a whole number from 1 to " + BuilderSession.MAX_SPEED + ".");
                return;
            }
            if (level < BuilderSession.NORMAL_SPEED || level > BuilderSession.MAX_SPEED) {
                Msg.bad(p, "Speed runs from 1 to " + BuilderSession.MAX_SPEED + ".");
                return;
            }
        }

        s.speed(level);
        sessions.applySpeed(p, s);
        sessions.save();
        Msg.good(p, level == BuilderSession.NORMAL_SPEED
                ? "Fly speed back to normal."
                : "Fly speed set to " + level + ".");
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return List.of();
        }
        List<String> options = new ArrayList<>(BuilderSession.MAX_SPEED + 1);
        for (int level = BuilderSession.NORMAL_SPEED; level <= BuilderSession.MAX_SPEED; level++) {
            options.add(Integer.toString(level));
        }
        options.add("reset");
        return options;
    }
}
