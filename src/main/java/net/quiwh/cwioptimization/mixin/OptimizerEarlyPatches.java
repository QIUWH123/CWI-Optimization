package net.quiwh.cwioptimization.mixin;

import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableRedirectNode;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Early repairs run before the original required injectors are prepared. No archive is edited. */
public final class OptimizerEarlyPatches {
    private static boolean registered;
    private OptimizerEarlyPatches() {}

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        MixinCancellerRegistrar.register((targets, mixin) -> switch (mixin) {
            // These injectors only force opaque alpha; the original item pipeline retains alpha.
            case "me.decce.gnetum.mixins.compat.ItemRendererMixin" -> OptimizerMixinPlugin.isEnabled("tint");
            // Stellar's compiled bounds include hanging packages; the narrower override hides them.
            case "net.liukrast.smartbounds.mixin.ChainConveyorBlockEntityMixin" -> OptimizerMixinPlugin.isEnabled("chain_bounds");
            // Replace its endpoint comparisons and count-only invalidation as one unit.
            case "net.liukrast.smartbounds.mixin.FactoryPanelBlockEntityMixin" -> OptimizerMixinPlugin.isEnabled("panel");
            // Registered atlas sprites remain owned by the resource reload/disposal lifecycle.
            case "com.konrados.ramization.mixin.MixinTextureAtlas" -> OptimizerMixinPlugin.isEnabled("ram");
            default -> false;
        });
        MixinAnnotationAdjusterRegistrar.register((targets, mixin, method, annotation) -> {
            if (mixin.equals("com.leclowndu93150.reallyfastload.mixin.client.ClientPacketListenerMixin")
                    && method.name.equals("reallyfastload$skipReceivingLevelScreen")
                    && annotation instanceof AdjustableRedirectNode redirect
                    && OptimizerMixinPlugin.isEnabled("loading")) {
                return redirect.withMethod(ignored -> List.of("startWaitingForNewLevel(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/gui/screens/ReceivingLevelScreen$Reason;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/resources/ResourceKey;)V"));
            }
            if (mixin.equals("org.codeberg.zenxarch.fastnoise.mixin.perf.noise.NoiseChunkGeneratorMixin")
                    && method.name.equals("zenxarch$populateNoise") && OptimizerMixinPlugin.isEnabled("noise")) {
                for (var instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode invoke) || !invoke.name.equals("doFill")) continue;
                    // Existing stack: generator, blender, structures, random, chunk, minCellY, cellHeight.
                    // Retain the fast path and supply only the missing original-lambda arguments.
                    var arguments = new InsnList();
                    arguments.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    arguments.add(new VarInsnNode(Opcodes.ILOAD, 4));
                    arguments.add(new VarInsnNode(Opcodes.ALOAD, 9));
                    method.instructions.insertBefore(invoke, arguments);
                    method.instructions.set(invoke, new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "net/quiwh/cwioptimization/NoiseFallback", "fill",
                            "(Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;IILnet/minecraft/world/level/levelgen/NoiseSettings;ILcom/llamalad7/mixinextras/injector/wrapoperation/Operation;)Lnet/minecraft/world/level/chunk/ChunkAccess;", false));
                }
            }
            return annotation;
        });
    }
}
