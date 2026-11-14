package net.quiwh.cwioptimization.mixin.optimizer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.quiwh.cwi.compat.processing.AdvancedProcessing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** AuxRecipeManager's override bypasses Core's parent-method return handler above 99 recipes. */
@Mixin(targets = "dev.shadowsoffire.fastsuite.AuxRecipeManager", remap = false)
public abstract class FastSuitePriorityMixin {
    @ModifyReturnValue(method = "getRecipesFor", at = @At("RETURN"))
    private <I extends RecipeInput, R extends Recipe<I>> List<RecipeHolder<R>> cwiOptimization$corePriorities(List<RecipeHolder<R>> original) {
        if (original.stream().noneMatch(recipe -> AdvancedProcessing.priority(recipe.value()) != 0)) return original;
        var sorted = new ArrayList<>(original);
        sorted.sort(Comparator.<RecipeHolder<R>>comparingInt(recipe -> AdvancedProcessing.priority(recipe.value())).reversed());
        return sorted;
    }
}
