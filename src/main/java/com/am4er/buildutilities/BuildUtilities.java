package com.am4er.buildutilities;

import com.am4er.buildutilities.command.BuilderCommand;
import com.am4er.buildutilities.command.sub.BrightSub;
import com.am4er.buildutilities.command.sub.GhostSub;
import com.am4er.buildutilities.command.sub.HideSub;
import com.am4er.buildutilities.command.sub.MeasureSub;
import com.am4er.buildutilities.command.sub.ReloadSub;
import com.am4er.buildutilities.command.sub.SpeedSub;
import com.am4er.buildutilities.command.sub.TimeSub;
import com.am4er.buildutilities.command.sub.ToolsSub;
import com.am4er.buildutilities.command.sub.WeatherSub;
import com.am4er.buildutilities.ghost.GhostListener;
import com.am4er.buildutilities.ghost.GhostService;
import com.am4er.buildutilities.measure.MeasureListener;
import com.am4er.buildutilities.measure.MeasureTool;
import com.am4er.buildutilities.session.SessionManager;
import com.am4er.buildutilities.tools.BuilderTool;
import com.am4er.buildutilities.tools.ToolCatalog;
import com.am4er.buildutilities.tools.ToolsMenuListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class BuildUtilities extends JavaPlugin {

    private static final int NOISY_SCAN_VOLUME = 20_000;

    private SessionManager sessions;
    private GhostService ghosts;
    private List<BuilderTool> tools = List.of();
    private Settings settings;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = Settings.read(getConfig());
        warnIfExpensive();

        PluginCommand builder = getCommand("builder");
        if (builder == null) {
            getLogger().severe("The builder command is missing from plugin.yml, disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        tools = ToolCatalog.load(getLogger());

        sessions = new SessionManager(this);
        sessions.load();

        ghosts = new GhostService(this, sessions);
        ghosts.start(settings);

        MeasureTool tape = new MeasureTool(this);

        PluginManager events = getServer().getPluginManager();
        events.registerEvents(new LifecycleListener(sessions, ghosts), this);
        events.registerEvents(new GhostListener(this, ghosts), this);
        events.registerEvents(new ToolsMenuListener(), this);
        events.registerEvents(new MeasureListener(tape, sessions), this);

        BuilderCommand handler = new BuilderCommand(List.of(
                new ToolsSub(this),
                new GhostSub(sessions, ghosts),
                new MeasureSub(tape, sessions),
                new TimeSub(sessions),
                new WeatherSub(sessions),
                new BrightSub(sessions),
                new HideSub(sessions),
                new SpeedSub(sessions),
                new ReloadSub(this)));
        builder.setExecutor(handler);
        builder.setTabCompleter(handler);

        for (Player p : getServer().getOnlinePlayers()) {
            sessions.onJoin(p);
            ghosts.onJoin(p);
        }

        getLogger().info(tools.size() + " builder tools ready.");
    }

    @Override
    public void onDisable() {
        if (ghosts != null) {
            ghosts.shutdown();
        }
        if (sessions != null) {
            sessions.shutdown();
        }
    }

    public List<BuilderTool> tools() {
        return tools;
    }

    public Settings reloadSettings() {
        reloadConfig();
        settings = Settings.read(getConfig());
        warnIfExpensive();
        ghosts.reload(settings);
        return settings;
    }

    private void warnIfExpensive() {
        int volume = settings.scanVolume();
        if (volume > NOISY_SCAN_VOLUME) {
            getLogger().warning("barrier-ghost.radius of " + settings.radius() + " scans " + volume
                    + " blocks per builder per sweep. Lower it if the server starts to stutter.");
        }
    }
}
