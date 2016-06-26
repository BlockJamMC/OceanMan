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
import java.nio.file.Files;
import java.util.Optional;
import java.util.Set;

public class WorldScanner {

    private static final int CHUNK_AREA = 16 * 16;
    private static final Set<BiomeType> OCEAN_BIOMES
            = Sets.newHashSet(BiomeTypes.OCEAN, BiomeTypes.DEEP_OCEAN, BiomeTypes.FROZEN_OCEAN);

    private World world;
    private int total;
    private int ocean;

    public Optional<Long> scanWorld() {
        try {
            OceanManPlugin.logger().info("Creating new world");
            WorldProperties wp = Sponge.getServer().createWorldProperties("oceanman", WorldArchetypes.OVERWORLD);
            long seed = wp.getSeed();
            OceanManPlugin.logger().info("Starting scan for seed " + seed);
            world = getServer().loadWorld("oceanman").get();
            total = 0;
            ocean = 0;

            boolean failed = false;
            outer:
            for (int r = 0; r <= OceanManPlugin.config().SCAN_RADIUS; r++) {
                OceanManPlugin.logger().info("Scanning radius level " + r);
                boolean intolerant = r <= OceanManPlugin.config().MIN_OCEAN_DISTANCE;
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
                try {
                    Files.delete(world.getDirectory());
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to delete world", ex);
                }
                world = null;
            }
        }
    }

    private boolean scanAndCheck(int x, int z, boolean intolerant) {
        Chunk chunk = world.loadChunk(world.getSpawnLocation().getChunkPosition().add(x, 0, z), true).get();
        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {
                total++;
                if (OCEAN_BIOMES.contains(chunk.getBiome(cx, cz))) {
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
        return (float) ocean / (float) total > OceanManPlugin.config().MAX_OCEAN_CONTENT;
    }

}
