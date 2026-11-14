package net.quiwh.cwioptimization.mixin.optimizer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.noxora.createfpsoptimizer.OptimizationController", remap = false)
public abstract class VisualCounterMixin {
    @Shadow @Final @Mutable private static Set<Long> COUNTED_FLYWHEEL_VISUALS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void cwiOptimization$concurrentVisuals(CallbackInfo ci) {
        COUNTED_FLYWHEEL_VISUALS = ConcurrentHashMap.newKeySet();
    }
}
