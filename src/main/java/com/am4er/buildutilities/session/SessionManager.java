package com.am4er.buildutilities.session;

import com.am4er.buildutilities.Perms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SessionManager {

    public static final float VANILLA_FLY_SPEED = 0.1f;

    private final Plugin plugin;
    private final SessionStore store;
    private final Map<UUID, BuilderSession> sessions = new HashMap<>();

    public SessionManager(Plugin plugin) {
        this.plugin = plugin;
        this.store = new SessionStore(plugin);
    }

    public void load() {
        sessions.putAll(store.load());
        if (!sessions.isEmpty()) {
            plugin.getLogger().info("Restored settings for " + sessions.size() + " builder(s).");
        }
    }

    public BuilderSession of(Player p) {
        return sessions.computeIfAbsent(p.getUniqueId(), _ -> new BuilderSession(p.getName()));
    }

    public @Nullable BuilderSession peek(Player p) {
        return sessions.get(p.getUniqueId());
    }

    public void save() {
        store.save(sessions);
    }

    public void onJoin(Player p) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            BuilderSession theirs = peek(other);
            if (!other.equals(p) && theirs != null && theirs.hiding()) {
                other.hidePlayer(plugin, p);
            }
        }

        BuilderSession s = peek(p);
        if (s == null) {
            return;
        }
        s.name(p.getName());
        if (enforce(p, s)) {
            save();
        }
        if (s.hasTime()) {
            applyTime(p, s);
        }
        if (s.weather() != null) {
            applyWeather(p, s);
        }
        if (s.bright()) {
            applyBright(p, s);
        }
        if (s.speed() != BuilderSession.NORMAL_SPEED) {
            applySpeed(p, s);
        }
        if (s.hiding()) {
            applyHiding(p, s);
        }
    }

    public void onQuit(Player p) {
        BuilderSession s = peek(p);
        if (s == null) {
            return;
        }
        s.mark(null);
        if (s.isDefault()) {
            sessions.remove(p.getUniqueId());
        }
    }

    public void applyTime(Player p, BuilderSession s) {
        if (s.hasTime()) {
            p.setPlayerTime(s.time(), false);
        } else {
            p.resetPlayerTime();
        }
    }

    public void applyWeather(Player p, BuilderSession s) {
        if (s.weather() != null) {
            p.setPlayerWeather(s.weather());
        } else {
            p.resetPlayerWeather();
        }
    }

    public void applyBright(Player p, BuilderSession s) {
        if (s.bright()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,
                    PotionEffect.INFINITE_DURATION, 0, true, false, false));
        } else {
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    public void applySpeed(Player p, BuilderSession s) {
        p.setFlySpeed(s.speed() / 10f);
    }

    public void applyHiding(Player p, BuilderSession s) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(p)) {
                continue;
            }
            if (s.hiding()) {
                p.hidePlayer(plugin, other);
            } else {
                p.showPlayer(plugin, other);
            }
        }
    }

    public void shutdown() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            BuilderSession s = peek(p);
            if (s != null) {
                revert(p, s);
            }
        }
        store.saveBlocking(sessions);
    }

    private void revert(Player p, BuilderSession s) {
        if (s.hasTime()) {
            p.resetPlayerTime();
        }
        if (s.weather() != null) {
            p.resetPlayerWeather();
        }
        if (s.bright()) {
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
        if (s.speed() != BuilderSession.NORMAL_SPEED) {
            p.setFlySpeed(VANILLA_FLY_SPEED);
        }
        if (s.hiding()) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(p)) {
                    p.showPlayer(plugin, other);
                }
            }
        }
    }

    private boolean enforce(Player p, BuilderSession s) {
        boolean changed = false;
        if (s.ghost() && !p.hasPermission(Perms.GHOST)) {
            s.ghost(false);
            changed = true;
        }
        if (s.hasTime() && !p.hasPermission(Perms.TIME)) {
            s.time(BuilderSession.WORLD_TIME);
            changed = true;
        }
        if (s.weather() != null && !p.hasPermission(Perms.WEATHER)) {
            s.weather(null);
            changed = true;
        }
        if (s.hiding() && !p.hasPermission(Perms.HIDE)) {
            s.hiding(false);
            changed = true;
        }
        if (s.bright() && !p.hasPermission(Perms.BRIGHT)) {
            s.bright(false);
            changed = true;
        }
        if (s.speed() != BuilderSession.NORMAL_SPEED && !p.hasPermission(Perms.SPEED)) {
            s.speed(BuilderSession.NORMAL_SPEED);
            changed = true;
        }
        return changed;
    }
}
