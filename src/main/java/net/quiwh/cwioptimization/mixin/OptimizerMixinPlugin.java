package net.quiwh.cwioptimization.mixin;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Validates resources without resolving optional game or client classes. */
public final class OptimizerMixinPlugin implements IMixinConfigPlugin {
    private record Requirement(String id, String version, Map<String, String> classes) {}
    private static final Map<String, Boolean> ENABLED = new HashMap<>();
    private static final Map<String, Requirement[]> REQUIREMENTS = loadRequirements();
    private static boolean earlyAvailable;

    private static Map<String, Requirement[]> loadRequirements() {
        try (var stream = OptimizerMixinPlugin.class.getResourceAsStream("/cwi_optimization-compatibility.json")) {
            if (stream == null) throw new IllegalStateException("Missing compatibility manifest");
            var type = new com.google.gson.reflect.TypeToken<Map<String, Requirement[]>>() {}.getType();
            return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), type);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Cannot read compatibility manifest", exception);
        }
    }

    public static synchronized boolean isEnabled(String family) {
        return ENABLED.computeIfAbsent(family, OptimizerMixinPlugin::check);
    }

    private static boolean check(String family) {
        var list = FMLLoader.getLoadingModList();
        var requirements = REQUIREMENTS.get(family);
        if (list == null || requirements == null || FMLLoader.versionInfo() == null
                || !"21.1.264".equals(FMLLoader.versionInfo().neoForgeVersion())) return false;
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (var requirement : requirements) {
                var info = list.getModFileById(requirement.id());
                if (info == null) return false;
                if (info.getMods().stream().noneMatch(mod -> mod.getModId().equals(requirement.id())
                        && mod.getVersion().toString().equals(requirement.version()))) {
                    LogUtils.getLogger().warn("CWI Optimization: skipping {} patch; unsupported {} version", family, requirement.id());
                    return false;
                }
                for (var entry : requirement.classes().entrySet()) {
                    var path = info.getFile().findResource(entry.getKey());
                    if (!Files.isRegularFile(path) || !HexFormat.of().formatHex(digest.digest(Files.readAllBytes(path))).equals(entry.getValue())) {
                        LogUtils.getLogger().warn("CWI Optimization: skipping {} patch; unsupported class {}", family, entry.getKey());
                        return false;
                    }
                }
            }
            return true;
        } catch (java.io.IOException | java.security.NoSuchAlgorithmException exception) {
            LogUtils.getLogger().warn("CWI Optimization: cannot validate {} patch", family, exception);
            return false;
        }
    }

    @Override public void onLoad(String mixinPackage) {
        // The API is optional; keep a bare Optimization installation loadable.
        try {
            if (!supportedEarlyApi()) {
                LogUtils.getLogger().warn("CWI Optimization: early repairs require the supported MixinSquared 0.3.7-beta.3 API");
                return;
            }
            Class.forName("com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster", false, getClass().getClassLoader());
            OptimizerEarlyPatches.register();
            earlyAvailable = true;
        } catch (ClassNotFoundException exception) {
            LogUtils.getLogger().warn("CWI Optimization: MixinSquared is absent; early optimizer repairs are unavailable");
        }
    }

    private static boolean supportedEarlyApi() {
        var hashes = Map.of(
                "adjuster/MixinAnnotationAdjusterRegistrar.class", "f131b433c131dc2bff244d2a341512028037b7bd7062bb1c935ced992409ac0e",
                "canceller/MixinCancellerRegistrar.class", "f7b9dad03fea32a3d5eacecb9d702139d47ebd8db19260381a05d895af2304d8");
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (var entry : hashes.entrySet()) {
                String resource = "com/bawnorton/mixinsquared/" + entry.getKey();
                var api = Class.forName(resource.substring(0, resource.length() - ".class".length()).replace('/', '.'),
                        false, OptimizerMixinPlugin.class.getClassLoader());
                // Class resources are module-local; read the bytes from the API's owning module.
                try (var stream = api.getResourceAsStream("/" + resource)) {
                    if (stream == null || !HexFormat.of().formatHex(digest.digest(stream.readAllBytes())).equals(entry.getValue())) return false;
                }
            }
            return true;
        } catch (ClassNotFoundException | java.io.IOException | java.security.NoSuchAlgorithmException exception) {
            return false;
        }
    }

    @Override public boolean shouldApplyMixin(String target, String mixin) {
        String name = mixin.substring(mixin.lastIndexOf('.') + 1);
        return switch (name) {
            case "VisualCounterMixin" -> isEnabled("counter");
            case "BufferLightMixin" -> isEnabled("light");
            case "PanelBoundsMixin" -> earlyAvailable && isEnabled("panel");
            case "FanCacheMixin" -> isEnabled("fan");
            case "FurnaceCacheMixin" -> isEnabled("furnace");
            case "RecipeEpochMixin" -> isEnabled("furnace") || isEnabled("fastsuite");
            case "FastSuiteCacheMixin", "FastSuiteListMixin" -> isEnabled("fastsuite");
            case "FastSuitePriorityMixin" -> isEnabled("fastsuite_priority");
            case "BlockEntityDistanceMixin" -> isEnabled("distance");
            case "GcSchedulerMixin" -> isEnabled("ram");
            default -> false;
        };
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> mine, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
