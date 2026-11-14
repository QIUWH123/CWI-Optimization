package net.quiwh.cwioptimization.mixin;

import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** No optional mod classes may be resolved before these binary/version checks. */
public final class MachineMixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, Boolean> ENABLED = new HashMap<>();

    public static synchronized boolean isEnabled(String family) {
        return ENABLED.computeIfAbsent(family, MachineMixinPlugin::check);
    }

    private static boolean check(String family) {
        if (!matches("create", "6.0.10", Map.of(
                "com/simibubi/create/content/kinetics/base/KineticBlockEntity.class", "3bce80f18d09ccff5e6c1b5cdbeeb2793f0a507006879b267148cde0df443572"))) return false;
        return switch (family) {
            case "ratatouille" -> matches("ratatouille", "1.4.0", Map.ofEntries(
                    Map.entry("org/forsteri/ratatouille/content/irrigation_tower/IrrigationTowerBlockEntity.class", "58db79a016d44698d1196f88efac2f84747f07c351de54db22d5ab16b9acbbf6"),
                    Map.entry("org/forsteri/ratatouille/content/spreader/SpreaderBlockEntity.class", "7ab6c38f02d16ee50c49024e2936ee48111a0f507bd314501dac987b593ef311")));
            case "optical" -> matches("create_optical", "0.4.2", Map.ofEntries(
                    Map.entry("net/lpcamors/optical/blocks/optical_source/GenericOpticalSourceBlockEntity.class", "d79c10df4f7f00df7351bd8987137962b45236f9b936e5f582ace32e7ac5ccb7"),
                    Map.entry("net/lpcamors/optical/blocks/optical_source/BeamHelper$BeamProperties.class", "845db1fe7b1df51478bbeef61bb7ba5864e15dd996356aa324328bfe96d16a20")));
            case "rubberworks" -> matches("rubberworks", "1.1.4", Map.ofEntries(
                    Map.entry("io/github/hadron13/rubberworks/blocks/sapper/SapperBlockEntity.class", "4aa088610fee9754a3cdec9466f7adab914bef884300eb78586c5b2bc7c86a69")));
            default -> false;
        };
    }

    private static boolean matches(String id, String version, Map<String, String> hashes) {
        var list = FMLLoader.getLoadingModList();
        var info = list == null ? null : list.getModFileById(id);
        if (info == null) return false;
        if (info.getMods().stream().noneMatch(mod -> mod.getModId().equals(id)
                && mod.getVersion().toString().equals(version))) {
            LogUtils.getLogger().warn("CWI Optimization: skipping {} integration; expected version {}", id, version);
            return false;
        }
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (var entry : hashes.entrySet()) {
                var path = info.getFile().findResource(entry.getKey());
                if (!Files.isRegularFile(path) || !HexFormat.of().formatHex(digest.digest(Files.readAllBytes(path))).equals(entry.getValue())) {
                    LogUtils.getLogger().warn("CWI Optimization: skipping {} integration; unsupported bytecode {}", id, entry.getKey());
                    return false;
                }
            }
            return true;
        } catch (IOException | NoSuchAlgorithmException exception) {
            LogUtils.getLogger().warn("CWI Optimization: cannot validate {} integration", id, exception);
            return false;
        }
    }

    @Override public boolean shouldApplyMixin(String target, String mixin) {
        if (mixin.contains(".ratatouille.")) return isEnabled("ratatouille");
        if (mixin.contains(".optical.")) return isEnabled("optical");
        if (mixin.contains(".rubberworks.")) return isEnabled("rubberworks");
        return false;
    }
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> mine, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
