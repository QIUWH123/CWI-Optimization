package net.quiwh.cwioptimization.mixin.optimizer;

import com.mojang.logging.LogUtils;
import java.util.concurrent.atomic.AtomicLong;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.konrados.ramization.memory.SmartGCScheduler", remap = false)
public abstract class GcSchedulerMixin {
    @Shadow @Final private AtomicLong lastGcTimeMs;
    @Unique private static boolean cwiOptimization$explained;

    @Inject(method = "triggerGC", at = @At("HEAD"), cancellable = true)
    private void cwiOptimization$leaveCollectionToJvm(long heapBefore, int heapPercentBefore, CallbackInfo ci) {
        // Preserve the polling cooldown and avoid reporting a collection that did not happen.
        lastGcTimeMs.set(System.currentTimeMillis());
        if (!cwiOptimization$explained) {
            cwiOptimization$explained = true;
            LogUtils.getLogger().info("CWI Optimization: leaving GC scheduling to the JVM; short ticks do not make an explicit full collection safe");
        }
        ci.cancel();
    }
}
