package net.quiwh.cwioptimization;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CwiOptimization.ID)
@PrefixGameTestTemplate(false)
public final class OptimizationLoadTest {
    @GameTest(template = "empty", templateNamespace = CwiOptimization.ID)
    public static void independentModLoads(GameTestHelper helper) {
        var container = ModList.get().getModContainerById(CwiOptimization.ID).orElseThrow();
        helper.assertTrue(container.getModInfo().getDisplayName().equals("CWI Optimization"),
                "the new independent mod is discovered with its own identity");
        helper.assertTrue(!ModList.get().isLoaded("cwi"),
                "the minimal verification environment does not require CWI Core");
        helper.succeed();
    }
}
