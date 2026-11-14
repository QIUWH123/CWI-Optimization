package net.quiwh.cwioptimization.mixin.optimizer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.moepus.createbetterfps.renderer.SodiumByteBuffer", remap = false)
public abstract class BufferLightMixin {
    @Shadow @Final private PoseStack transforms;

    @Inject(method = "getLight(Lnet/minecraft/world/level/BlockAndTintGetter;Lorg/joml/Vector3f;)I", at = @At("HEAD"), cancellable = true)
    private static void cwiOptimization$currentLight(BlockAndTintGetter world, Vector3f position, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(LevelRenderer.getLightColor(world, BlockPos.containing(position.x, position.y, position.z)));
    }

    @ModifyArg(method = "defaultRenderInto", at = @At(value = "INVOKE",
            target = "Lcom/moepus/createbetterfps/renderer/SodiumByteBuffer;getLight(Lorg/joml/Vector3f;)I"), index = 0)
    private Vector3f cwiOptimization$worldLightPosition(Vector3f position) {
        return position.mulPosition(transforms.last().pose());
    }
}
