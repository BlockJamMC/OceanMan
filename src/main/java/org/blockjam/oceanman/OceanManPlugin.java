package org.blockjam.oceanman;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.blockjam.oceanman.util.ConfigHandler;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Main plugin class.
 */
@Plugin(id = "oceanman", name = "OceanMan", version = "1.0.0-SNAPSHOT")
public class OceanManPlugin {

    private static OceanManPlugin instance;

    private static File seedStore;

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;
    @Inject @DefaultConfig(sharedRoot = false) private File config;
    @Inject @ConfigDir(sharedRoot = false) private File configDir;
    @Inject private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    private ConfigHandler configHandler;

    @Listener
    public void onPreInitialization(GamePreInitializationEvent event) {
        instance = this;
        seedStore = new File(configDir, "seeds.txt");
        try {
            configHandler = new ConfigHandler(config, configLoader);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load config", ex);
        }
    }

    public void onStarted(GameStartedServerEvent event) {
        WorldScanner scanner = new WorldScanner();
        Set<Long> seeds = new HashSet<>();
        while (seeds.size() < config().DESIRED_SEEDS) {
            Optional<Long> seed = scanner.scanWorld();
            if (seed.isPresent()) {
                seeds.add(seed.get());
                logger().info("Stored seed (current count: " + seeds.size() + ")");
            }
        }
        logger().info("Saving " + seeds.size() + " to disk");
        try (FileWriter writer = new FileWriter(seedStore)) {
            seeds.forEach(s -> {
                try {
                    writer.write(s + "\n");
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write seed to disk", ex);
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write seeds to disk", ex);
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

    public static ConfigHandler config() {
        return instance().configHandler;
    }

}
