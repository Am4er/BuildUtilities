package com.am4er.buildutilities;

import com.am4er.buildutilities.ghost.GhostService;
import com.am4er.buildutilities.session.SessionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class LifecycleListener implements Listener {

    private final SessionManager sessions;
    private final GhostService ghosts;

    public LifecycleListener(SessionManager sessions, GhostService ghosts) {
        this.sessions = sessions;
        this.ghosts = ghosts;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        sessions.onJoin(event.getPlayer());
        ghosts.onJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        ghosts.onQuit(event.getPlayer());
        sessions.onQuit(event.getPlayer());
    }
}
