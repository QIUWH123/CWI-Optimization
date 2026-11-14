package net.quiwh.cwioptimization.mixin.ratatouille;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.Fluids;
import org.forsteri.ratatouille.content.irrigation_tower.IrrigationTowerBlockEntity;
import org.forsteri.ratatouille.entry.CRFluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IrrigationTowerBlockEntity.class, remap = false)
abstract class IrrigationTowerMixin {
    @Inject(method = "isNearWater", at = @At("HEAD"), cancellable = true)
    private static void cwi$indexedTowers(LevelReader reader, BlockPos soil,
                                         CallbackInfoReturnable<Boolean> farmlandCallback, CallbackInfo callback) {
        // Preserve the upstream scan for unusual readers without a live chunk index.
        if (!(reader instanceof Level level)) return;
        for (int cx = (soil.getX() - 8) >> 4; cx <= (soil.getX() + 8) >> 4; cx++) {
            for (int cz = (soil.getZ() - 8) >> 4; cz <= (soil.getZ() + 8) >> 4; cz++) {
                var chunk = level.getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                // Includes pending NBT positions, so the first query after loading works too.
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    if (pos.getX() < soil.getX() - 8 || pos.getX() > soil.getX() + 8
                            || pos.getZ() < soil.getZ() - 8 || pos.getZ() > soil.getZ() + 8
                            || pos.getY() < soil.getY() || pos.getY() > soil.getY() + 2) continue;
                    if (level.getBlockEntity(pos) instanceof IrrigationTowerBlockEntity tower) {
                        var fluid = tower.getTankInventory().getFluid();
                        if (!fluid.isEmpty() && (fluid.getFluid().isSame(Fluids.WATER)
                                || fluid.getFluid().isSame(CRFluids.COMPOST_TEA.get()))) {
                            callback.cancel();
                            farmlandCallback.setReturnValue(true);
                            return;
                        }
                    }
                }
            }
        }
        // This helper belongs to the existing farmland injection: cancelling its empty
        // return leaves the farmland callback untouched, allowing vanilla water checks.
        callback.cancel();
    }
}
