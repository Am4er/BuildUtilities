package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.ToggleSub;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class HideSub extends ToggleSub {

    public HideSub(SessionManager sessions) {
        super(sessions);
    }

    @Override public String name() { return "hide"; }
    @Override public List<String> aliases() { return List.of("solo"); }
    @Override public String permission() { return Perms.HIDE; }
    @Override public String blurb() { return "Hide every other player from view."; }
    @Override protected String what() { return "Player hiding"; }
    @Override protected String hint() { return "Everyone else is invisible to you. They can still see you."; }
    @Override protected boolean read(BuilderSession s) { return s.hiding(); }

    @Override
    protected void write(Player p, BuilderSession s, boolean on) {
        s.hiding(on);
        sessions.applyHiding(p, s);
    }
}
