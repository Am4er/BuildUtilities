package com.am4er.buildutilities.command.sub;

import com.am4er.buildutilities.Perms;
import com.am4er.buildutilities.command.ToggleSub;
import com.am4er.buildutilities.ghost.GhostService;
import com.am4er.buildutilities.session.BuilderSession;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class GhostSub extends ToggleSub {

    private final GhostService ghosts;

    public GhostSub(SessionManager sessions, GhostService ghosts) {
        super(sessions);
        this.ghosts = ghosts;
    }

    @Override public String name() { return "ghost"; }
    @Override public List<String> aliases() { return List.of("barrier", "noclip"); }
    @Override public String permission() { return Perms.GHOST; }
    @Override public String blurb() { return "Walk through barrier blocks."; }
    @Override protected String what() { return "Ghost mode"; }

    @Override
    protected String hint() {
        return "Barriers near you are hidden from your client only, nobody else sees a change.";
    }

    @Override protected boolean read(BuilderSession s) { return s.ghost(); }

    @Override
    protected void write(Player p, BuilderSession s, boolean on) {
        if (on) {
            ghosts.enable(p);
        } else {
            ghosts.disable(p);
        }
    }
}
