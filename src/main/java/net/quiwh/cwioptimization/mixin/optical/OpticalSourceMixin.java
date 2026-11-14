package net.quiwh.cwioptimization.mixin.optical;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity.BeamSection;
import net.quiwh.cwioptimization.OpticalSnapshot;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GenericOpticalSourceBlockEntity.class, remap = false)
abstract class OpticalSourceMixin {
    @Shadow @Final public List<BeamSection> sections;
    @Shadow @Final public Map<BlockPos, BeamProperties> activators;
    @Shadow public boolean shouldSendClient;
    @Unique private OpticalSnapshot cwi$lastSent;

    @Inject(method = "tick", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/lpcamors/optical/blocks/optical_source/GenericOpticalSourceBlockEntity;shouldSendClient:Z"), require = 1, allow = 1)
    private void cwi$syncChangedState(CallbackInfo callback) {
        if (!(((BlockEntity) (Object) this).getLevel() instanceof ServerLevel)) return;
        shouldSendClient = cwi$lastSent == null || !cwi$lastSent.sections().equals(sections)
                || !cwi$lastSent.activators().equals(activators);
        if (shouldSendClient) cwi$lastSent = OpticalSnapshot.capture(sections, activators);
    }
}
