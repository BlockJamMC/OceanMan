package org.blockjam.oceanman;

import static org.spongepowered.api.Sponge.getServer;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetypes;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldScanner {

    private static final int CHUNK_AREA = 16 * 16;

    private AtomicInteger total = new AtomicInteger();
    private AtomicInteger ocean = new AtomicInteger();

    public Optional<Long> scanWorld() {
        try {
            OceanManPlugin.logger().info("Creating new world");
            long seed = Sponge.getServer().createWorldProperties("oceanman", WorldArchetypes.OVERWORLD).getSeed();
            OceanManPlugin.logger().info("Starting scan for seed " + seed);
            World world = getServer().loadWorld("oceanman").get();
            Vector3i spawnChunk = world.getSpawnLocation().getChunkPosition();

            int scannedBlocks = 0;
            int oceanBlocks = 0;
            for (int r = 0; r <= OceanManPlugin.config().SCAN_RADIUS; r++) {
                boolean intolerant = r <= OceanManPlugin.config().MIN_OCEAN_DISTANCE;
                for (int i = -r; i <= r; i++) {
                    oceanBlocks += scanChunk(new Vector2i(-r, i), intolerant);
                    oceanBlocks += scanChunk(new Vector2i(r, i), intolerant);
                    if (r != i) {
                        oceanBlocks += scanChunk(new Vector2i(i, -r), intolerant);
                        oceanBlocks += scanChunk(new Vector2i(i, r), intolerant);
                    }
                }
            }
        } catch (IOException ex) {
            OceanManPlugin.logger().error("Failed to create world");
            ex.printStackTrace();
        }
        return null; //TODO
    }

    private int scanChunk(Vector2i chunk, boolean breakOnOcean) {
        return 0;
    }

    private boolean scanAndCheck(int x, int z, boolean intolerant) {
        //TODO
        return false;
    }

    public boolean check(int foundOceanBlocks, boolean intolerant) {
        ocean.addAndGet(foundOceanBlocks);
        total.addAndGet(CHUNK_AREA);
        if ((double) ocean.get() / (double) total.get() > OceanManPlugin.config().MAX_OCEAN_CONTENT) {
            return false;
        }
        return false; //TODO
    }

}
