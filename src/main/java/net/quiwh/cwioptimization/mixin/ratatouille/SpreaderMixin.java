package net.quiwh.cwioptimization.mixin.ratatouille;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.forsteri.ratatouille.content.spreader.SpreaderBlockEntity;
import org.forsteri.ratatouille.entry.CRItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SpreaderBlockEntity.class, remap = false)
abstract class SpreaderMixin {
    @Shadow public ItemStackHandler inventory;
    @Shadow public abstract void addCorpBuffer(ArrayList<BlockPos> crops, BlockPos pos);

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/BlockPos;betweenClosed(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;"), require = 1, allow = 1)
    private Iterable<BlockPos> cwi$cropVolume(BlockPos min, BlockPos max) {
        return inventory.getStackInSlot(0).is(CRItems.RIPEN_MATTER.get())
                ? BlockPos.betweenClosed(min, max) : List.of();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lorg/forsteri/ratatouille/content/spreader/SpreaderBlockEntity;addCorpBuffer(Ljava/util/ArrayList;Lnet/minecraft/core/BlockPos;)V"), require = 2, allow = 2)
    private void cwi$cropTargets(SpreaderBlockEntity source, ArrayList<BlockPos> crops, BlockPos pos) {
        if (inventory.getStackInSlot(0).is(CRItems.RIPEN_MATTER.get())) addCorpBuffer(crops, pos);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"), require = 2, allow = 2)
    private <T extends Entity> List<T> cwi$animalTargets(Level level, Class<T> type, AABB bounds) {
        return inventory.getStackInSlot(0).is(CRItems.MATURE_MATTER.get())
                ? level.getEntitiesOfClass(type, bounds) : List.of();
    }
}
