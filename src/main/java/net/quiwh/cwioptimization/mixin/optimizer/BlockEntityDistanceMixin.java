package net.quiwh.cwioptimization.mixin.optimizer;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.quiwh.cwioptimization.RenderBoundsDistance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.srrapero720.chloride.impl.EntityCulling", remap = false)
public abstract class BlockEntityDistanceMixin {
    @Shadow @Final public static boolean SABLE_I;

    @Inject(method = "isEntityInRange(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;II)Z",
            at = @At("RETURN"), cancellable = true)
    private static void cwiOptimization$visibleExtents(BlockEntity tile, Vec3 camera, int maxHeight, int maxDistanceSquared, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || SABLE_I) return;
        var renderer = net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
        if (renderer == null) return;
        var bounds = renderer.getRenderBoundingBox(tile);
        var unit = new AABB(tile.getBlockPos());
        // Ordinary blocks retain the configured center-distance behavior exactly.
        if (bounds != null && !unit.minmax(bounds).equals(unit) && RenderBoundsDistance.inRange(bounds, camera, maxHeight, maxDistanceSquared)) {
            cir.setReturnValue(true);
        }
    }
}
