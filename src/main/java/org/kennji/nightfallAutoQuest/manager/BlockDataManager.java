package org.kennji.nightfallAutoQuest.manager;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.kennji.nightfallAutoQuest.NightfallAutoQuest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockDataManager {
    private final NightfallAutoQuest plugin;
    // Map<ChunkKey, Set<RelativePosition>>
    // RelativePosition = (x << 24) | (y << 8) | z (supports y from -256 to 1024 approx)
    private final Map<Long, Set<Integer>> placedBlocks = new ConcurrentHashMap<>();

    public BlockDataManager(@NotNull NightfallAutoQuest plugin) {
        this.plugin = plugin;
    }

    public void markAsPlaced(@NotNull Block block) {
        if (!plugin.getConfigManager().getConfig().getBoolean("anti-abuse.prevent-placed-blocks", true)) return;

        long chunkKey = getChunkKey(block.getChunk());
        placedBlocks.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(getRelativeKey(block));
    }

    public boolean isPlayerPlaced(@NotNull Block block) {
        if (!plugin.getConfigManager().getConfig().getBoolean("anti-abuse.prevent-placed-blocks", true)) return false;

        long chunkKey = getChunkKey(block.getChunk());
        Set<Integer> bits = placedBlocks.get(chunkKey);
        return bits != null && bits.contains(getRelativeKey(block));
    }

    public void remove(@NotNull Block block) {
        long chunkKey = getChunkKey(block.getChunk());
        Set<Integer> bits = placedBlocks.get(chunkKey);
        if (bits != null) {
            bits.remove(getRelativeKey(block));
            if (bits.isEmpty()) {
                placedBlocks.remove(chunkKey);
            }
        }
    }

    public void clearChunk(long chunkKey) {
        placedBlocks.remove(chunkKey);
    }

    private long getChunkKey(@NotNull Chunk chunk) {
        return ((long) chunk.getX() << 32) | (chunk.getZ() & 0xFFFFFFFFL);
    }

    private int getRelativeKey(@NotNull Block block) {
        // x: 0-15 (4 bits), z: 0-15 (4 bits), y: -64 to 320 (around 9-10 bits)
        // Let's just use absolute relative if inside chunk? 
        // No, let's just pack them: (x & 0xF) | ((z & 0xF) << 4) | (y << 8)
        return (block.getX() & 0xF) | ((block.getZ() & 0xF) << 4) | (block.getY() << 8);
    }
}
