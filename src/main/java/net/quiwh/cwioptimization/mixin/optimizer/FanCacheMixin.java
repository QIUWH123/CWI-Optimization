package net.quiwh.cwioptimization.mixin.optimizer;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import java.util.List;
import net.quiwh.cwioptimization.MergedCacheFields;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = AirCurrent.class, priority = 900, remap = false)
public abstract class FanCacheMixin {
    @Shadow public List<?> segments;
    @Unique private List<?> cwiOptimization$cachedSegments;

    @WrapMethod(method = "getTypeAt")
    private FanProcessingType cwiOptimization$publishedSegments(float offset, Operation<FanProcessingType> original) {
        synchronized (this) {
            if (segments != cwiOptimization$cachedSegments) {
                cwiOptimization$cachedSegments = segments;
                MergedCacheFields.clearFan(this);
            }
            return original.call(offset);
        }
    }

    @WrapMethod(method = "rebuild")
    private void cwiOptimization$serializeRebuild(Operation<Void> original) {
        synchronized (this) {
            try {
                original.call();
            } finally {
                // AsyncParticles publishes at RETURN, after native handler queries used the old list.
                cwiOptimization$cachedSegments = segments;
                MergedCacheFields.clearFan(this);
            }
        }
    }
}
