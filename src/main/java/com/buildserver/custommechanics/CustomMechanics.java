package com.buildserver.custommechanics;

import com.buildserver.custommechanics.command.BuilderCommand;
import com.buildserver.custommechanics.ghost.BarrierGhostListener;
import com.buildserver.custommechanics.ghost.BarrierGhostManager;
import com.buildserver.custommechanics.tools.BuilderTool;
import com.buildserver.custommechanics.tools.BuilderToolRegistry;
import com.buildserver.custommechanics.tools.ToolsMenuListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class CustomMechanics extends JavaPlugin {

    private List<BuilderTool> tools = List.of();
    private BarrierGhostManager ghostManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        tools = BuilderToolRegistry.build(getLogger());

        ghostManager = new BarrierGhostManager(this);
        ghostManager.start();

        getServer().getPluginManager().registerEvents(new ToolsMenuListener(), this);
        getServer().getPluginManager().registerEvents(new BarrierGhostListener(this, ghostManager), this);

        PluginCommand builder = getCommand("builder");
        if (builder == null) {
            getLogger().severe("The 'builder' command is missing from plugin.yml - disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        BuilderCommand executor = new BuilderCommand(this, ghostManager);
        builder.setExecutor(executor);
        builder.setTabCompleter(executor);

        getLogger().info("Loaded " + tools.size() + " builder tools.");
    }

    @Override
    public void onDisable() {
        if (ghostManager != null) {
            ghostManager.shutdown();
        }
    }

    public List<BuilderTool> tools() {
        return tools;
    }
}
