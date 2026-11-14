package net.quiwh.cwioptimization.mixin.optimizer;

import java.util.Map;
import net.quiwh.cwioptimization.RecipeEpoch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.shadowsoffire.fastsuite.AuxRecipeManager", remap = false)
public abstract class FastSuiteCacheMixin {
    @Shadow @Final private Map<?, ?> cachedRecipeListMap;
    @Unique private long cwiOptimization$cachedEpoch = Long.MIN_VALUE;

    @Inject(method = "getCachedRecipeList", at = @At("HEAD"))
    private void cwiOptimization$expireRecipes(CallbackInfoReturnable<?> cir) {
        long epoch = ((RecipeEpoch) this).cwiOptimization$recipeEpoch();
        synchronized (cachedRecipeListMap) {
            if (cwiOptimization$cachedEpoch != epoch) {
                cachedRecipeListMap.clear();
                cwiOptimization$cachedEpoch = epoch;
            }
        }
    }
}
