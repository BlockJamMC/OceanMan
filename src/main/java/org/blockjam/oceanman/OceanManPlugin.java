/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016, BlockJam <https://blockjam.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockjam.oceanman;

import org.blockjam.oceanman.util.ConfigHandler;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
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
import java.nio.file.Files;
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
    @Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;

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

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        WorldScanner scanner = new WorldScanner();
        int count = 0;
        while (count < config().desiredSeeds) {
            Optional<Long> seed = scanner.scanWorld();
            if (seed.isPresent()) {
                try {
                    if (!seedStore.exists()) {
                        Files.createFile(seedStore.toPath());
                    }
                    try (FileWriter writer = new FileWriter(seedStore)) {
                        writer.write(seed.get() + "\n");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to write seeds to disk", ex);
                }
                count++;
                logger().info("Stored seed (current count: " + count + ")");
            }
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
