package org.blockjam.oceanman;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.blockjam.oceanman.util.ConfigHandler;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;

/**
 * Main plugin class.
 */
@Plugin(id = "oceanman", name = "OceanMan", version = "1.0.0-SNAPSHOT")
public class OceanManPlugin {

    private static OceanManPlugin instance;

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;
    @Inject private File config;
    @Inject private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    private ConfigHandler configHandler;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        instance = this;
        try {
            configHandler = new ConfigHandler(config, configLoader);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load config", ex);
        }
    }

    public static OceanManPlugin instance() {
        return instance;
    }

    public static PluginContainer plugin() {
        return instance().plugin;
    }

    public static Logger logger() {
        return instance().logger;
    }

}
