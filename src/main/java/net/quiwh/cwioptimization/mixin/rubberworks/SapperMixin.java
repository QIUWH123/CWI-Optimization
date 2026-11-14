package net.quiwh.cwioptimization.mixin.rubberworks;

import io.github.hadron13.rubberworks.blocks.sapper.SapperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = SapperBlockEntity.class, remap = false)
abstract class SapperMixin {
    @Shadow private boolean cached;
    @Shadow private boolean valid;

    @Inject(method = "checkValidity", at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void cwi$rejectInvalidLog(CallbackInfo callback, BlockPos target, BlockState state) {
        if (!SapperBlockEntity.TreeType.isValidLog(state)) {
            cached = false;
            valid = false;
            callback.cancel();
        }
    }
}
