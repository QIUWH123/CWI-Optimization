package net.quiwh.cwioptimization;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Re-enters the wrapped fill lambda so Noisium retains its section recount/finally. */
public final class NoiseFallback {
    private NoiseFallback() {}

    public static ChunkAccess fill(NoiseBasedChunkGenerator generator, Blender blender,
            StructureManager structures, RandomState random, ChunkAccess chunk, int minCellY,
            int cellHeight, NoiseSettings settings, int minY, Operation<ChunkAccess> original) {
        return original.call(chunk, cellHeight, settings, minY, blender, structures, random, minCellY);
    }
}
