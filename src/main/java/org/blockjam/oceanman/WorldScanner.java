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

import static org.spongepowered.api.Sponge.getServer;

import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class WorldScanner {

    private static final Set<BiomeType> OCEAN_BIOMES
            = Sets.newHashSet(BiomeTypes.OCEAN, BiomeTypes.DEEP_OCEAN, BiomeTypes.FROZEN_OCEAN);

    private World world;
    private int total;
    private int ocean;

    public Optional<Long> scanWorld() {
        WorldProperties wp = null;
        try {
            while (Sponge.getServer().getWorldProperties("oceanman").isPresent()) {
            }
            OceanManPlugin.logger().info("Creating new world");
            wp = Sponge.getServer().createWorldProperties("oceanman", WorldArchetypes.OVERWORLD);
            Sponge.getServer().saveWorldProperties(wp);
            long seed = wp.getSeed();
            OceanManPlugin.logger().info("Starting scan for seed " + seed);
            world = getServer().loadWorld("oceanman").get();
            total = 0;
            ocean = 0;

            boolean failed = false;
            outer:
            for (int r = 0; r <= OceanManPlugin.config().scanRadius; r++) {
                OceanManPlugin.logger().info("Scanning radius level " + r);
                boolean intolerant = r <= OceanManPlugin.config().minOceanDistance;
                // this isn't even close to DRY but idk how else to write it
                for (int i = -r; i <= r; i++) {
                    if (!scanAndCheck(-r, i, intolerant)) {
                        failed = true;
                        break outer;
                    }
                    if (!scanAndCheck(r, i, intolerant)) {
                        failed = true;
                        break outer;
                    }
                    if (r != i && r != -i) {
                        if (!scanAndCheck(i, -r, intolerant)) {
                            failed = true;
                            break outer;
                        }
                        if (!scanAndCheck(i, r, intolerant)) {
                            failed = true;
                            break outer;
                        }
                    }
                }
            }
            if (failed) {
                OceanManPlugin.logger().info("World failed requirements; discarding");
            }
            return !failed ? Optional.of(seed) : Optional.empty();
        } catch (IOException ex) {
            OceanManPlugin.logger().error("Failed to create world");
            ex.printStackTrace();
            return Optional.empty();
        } finally {
            if (world != null) {
                Sponge.getServer().unloadWorld(world);
                world = null;
            }
            if (wp != null) {
                wp.setEnabled(false);
                Sponge.getServer().deleteWorld(wp);
            }
        }
    }

    private boolean scanAndCheck(int x, int z, boolean intolerant) {
        Chunk chunk = world.loadChunk(world.getSpawnLocation().getChunkPosition().add(x, 0, z), true).get();
        int minX = chunk.getPosition().getX() * 16;
        int minZ = chunk.getPosition().getZ() * 16;
        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {
                total++;
                if (OCEAN_BIOMES.contains(chunk.getLocation(minX + cx, 0, minZ + cz).getBiome())) {
                    if (intolerant) {
                        return false;
                    }
                    ocean++;
                    if (!check()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean check() {
        return (float) ocean / (float) total > OceanManPlugin.config().maxOceanContent;
    }

}
