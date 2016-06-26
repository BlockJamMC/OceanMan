package org.blockjam.oceanman.util;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * Configuration handler.
 */
public class ConfigHandler {

    private final File configFile;
    private final ConfigurationLoader<?> loader;
    private final ConfigurationNode config;

    public final int MIN_OCEAN_DISTANCE;
    public final double MAX_OCEAN_CONTENT;

    public ConfigHandler(File configFile, ConfigurationLoader<CommentedConfigurationNode> loader) throws IOException {
        this.configFile = configFile;
        this.loader = loader;
        this.config = loader.load();
        loadDefaults();

        MIN_OCEAN_DISTANCE = config.getNode("min-ocean-distance").getInt();
        MAX_OCEAN_CONTENT = config.getNode("max-ocean-content").getDouble();
    }

    public void loadDefaults() throws IOException {
        URL defaultsInJarURL = ConfigHandler.class.getResource("/default.conf");
        ConfigurationLoader defaultsLoader = HoconConfigurationLoader.builder().setURL(defaultsInJarURL).build();
        ConfigurationNode defaults = defaultsLoader.load();

        if (!configFile.exists()) {
            Files.createFile(configFile.toPath());
        }
        config.mergeValuesFrom(defaults);
        loader.save(config);
    }

}
