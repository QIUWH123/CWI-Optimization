package net.quiwh.cwioptimization;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/** Independent home for CWI modpack performance and compatibility work. */
@Mod(CwiOptimization.ID)
public final class CwiOptimization {
    public static final String ID = "cwi_optimization";
    public static final String NAME = "CWI Optimization";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CwiOptimization() {
        if (net.quiwh.cwioptimization.mixin.MachineMixinPlugin.isEnabled("optical")) {
            OpticalEvents.register();
        }
        LOGGER.info("{} loaded with version-gated machine optimizations.", NAME);
    }
}
