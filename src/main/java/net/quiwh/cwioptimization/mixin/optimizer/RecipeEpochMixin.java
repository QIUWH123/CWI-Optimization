package net.quiwh.cwioptimization.mixin.optimizer;

import net.minecraft.world.item.crafting.RecipeManager;
import net.quiwh.cwioptimization.RecipeEpoch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public abstract class RecipeEpochMixin implements RecipeEpoch {
    @Unique private volatile long cwiOptimization$epoch;
    @Override public long cwiOptimization$recipeEpoch() { return cwiOptimization$epoch; }

    @Inject(method = {"apply", "replaceRecipes"}, at = @At("RETURN"))
    private void cwiOptimization$recipesChanged(CallbackInfo ci) {
        cwiOptimization$epoch++;
    }
}
