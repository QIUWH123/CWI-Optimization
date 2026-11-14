package net.quiwh.cwioptimization.mixin.optimizer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Retains parallel matching while restoring the source collection's first-match order. */
@Mixin(targets = "dev.shadowsoffire.fastsuite.CachedRecipeList", remap = false)
public abstract class FastSuiteListMixin<C extends RecipeInput, T extends Recipe<C>> {
    @Shadow @Final private List<RecipeHolder<T>> serialRecipes;
    @Unique private final Map<RecipeHolder<T>, Integer> cwiOptimization$serialBefore = new IdentityHashMap<>();
    @Unique private final Map<RecipeHolder<T>, Integer> cwiOptimization$sourceOrder = new IdentityHashMap<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cwiOptimization$rememberOrder(RecipeType<T> type, Collection<RecipeHolder<T>> recipes, CallbackInfo ci) {
        var serial = Collections.newSetFromMap(new IdentityHashMap<RecipeHolder<T>, Boolean>());
        serial.addAll(serialRecipes);
        int preceding = 0;
        int position = 0;
        for (var recipe : recipes) {
            cwiOptimization$sourceOrder.put(recipe, position++);
            if (serial.contains(recipe)) preceding++;
            else cwiOptimization$serialBefore.put(recipe, preceding);
        }
    }

    @ModifyArg(method = "getRecipesFor", at = @At(value = "INVOKE", target = "Ljava/util/Collections;sort(Ljava/util/List;Ljava/util/Comparator;)V"), index = 1)
    private Comparator<RecipeHolder<T>> cwiOptimization$stableTies(Comparator<RecipeHolder<T>> sorter) {
        return sorter.thenComparingInt(recipe -> cwiOptimization$sourceOrder.get(recipe));
    }

    @ModifyReturnValue(method = "getRecipeFor", at = @At("RETURN"))
    private Optional<RecipeHolder<T>> cwiOptimization$firstInSourceOrder(Optional<RecipeHolder<T>> result, C input, Level level) {
        if (result.isEmpty()) return result;
        Integer limit = cwiOptimization$serialBefore.get(result.get());
        if (limit == null) return result; // Native serial search already found its first match.
        for (int i = 0; i < limit; i++) {
            var recipe = serialRecipes.get(i);
            if (recipe.value().matches(input, level)) return Optional.of(recipe);
        }
        return result;
    }

    @ModifyExpressionValue(method = "getRecipesFor", at = @At(value = "INVOKE", target = "Ljava/util/Collections;emptyList()Ljava/util/List;"))
    private List<RecipeHolder<T>> cwiOptimization$mutableFallback(List<RecipeHolder<T>> original) {
        // FastSuite appends serial matches and sorts this list after a timeout/interruption.
        return new ArrayList<>();
    }
}
