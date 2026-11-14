package net.quiwh.cwioptimization.mixin.optimizer;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import java.util.EnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FactoryPanelBlockEntity.class, priority = 2100, remap = false)
public abstract class PanelBoundsMixin extends SmartBlockEntity {
    @Shadow public EnumMap<PanelSlot, FactoryPanelBehaviour> panels;
    protected PanelBoundsMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) { super(type, pos, state); }

    @Inject(method = "createRenderBoundingBox", at = @At("HEAD"), cancellable = true)
    private void cwiOptimization$completeEndpoints(CallbackInfoReturnable<AABB> cir) {
        AABB bounds = new AABB(getBlockPos());
        for (var panel : panels.values()) {
            for (var endpoint : panel.targetedBy.keySet()) bounds = bounds.minmax(new AABB(endpoint.pos()));
            for (var endpoint : panel.targetedByLinks.keySet()) bounds = bounds.minmax(new AABB(endpoint));
        }
        cir.setReturnValue(bounds);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void cwiOptimization$refreshAfterRead(CompoundTag tag, HolderLookup.Provider registries, boolean packet, CallbackInfo ci) {
        if (level != null && level.isClientSide) invalidateRenderBoundingBox();
    }
}
