package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.ToggleSub;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class BrightSub extends ToggleSub {

    public BrightSub(SessionManager sessions) {
        super(sessions);
    }

    @Override public String name() { return "bright"; }
    @Override public List<String> aliases() { return List.of("nightvision", "nv"); }
    @Override public String permission() { return Perms.BRIGHT; }
    @Override public String blurb() { return "Light up dark interiors while you work."; }
    @Override protected String what() { return "Brightness"; }
    @Override protected boolean read(BuilderSession s) { return s.bright(); }

    @Override
    protected void write(Player p, BuilderSession s, boolean on) {
        s.bright(on);
        sessions.applyBright(p, s);
    }
}
