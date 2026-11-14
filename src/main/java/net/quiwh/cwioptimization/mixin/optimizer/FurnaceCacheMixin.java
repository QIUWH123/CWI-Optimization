package net.quiwh.cwioptimization.mixin.optimizer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.quiwh.cwioptimization.RecipeEpoch;
import net.quiwh.cwioptimization.MergedCacheFields;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.class, priority = 900, remap = false)
public abstract class FurnaceCacheMixin extends BlockEntity {
    @Unique private RecipeManager cwiOptimization$manager;
    @Unique private long cwiOptimization$epoch;

    protected FurnaceCacheMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Dynamic("FastFurnace")
    @Inject(method = "getRecipe()Lnet/minecraft/world/item/crafting/RecipeHolder;", at = @At("HEAD"))
    private void cwiOptimization$invalidateRecipe(CallbackInfoReturnable<RecipeHolder<AbstractCookingRecipe>> cir) {
        if (level == null) return;
        var manager = level.getRecipeManager();
        long epoch = ((RecipeEpoch) manager).cwiOptimization$recipeEpoch();
        if (manager != cwiOptimization$manager || epoch != cwiOptimization$epoch) {
            cwiOptimization$manager = manager;
            cwiOptimization$epoch = epoch;
            MergedCacheFields.clearFurnace(this);
        }
    }
}
