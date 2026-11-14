package net.quiwh.cwioptimization;

import java.lang.reflect.Field;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

/** Mixin cannot validate @Shadow fields introduced by another mixin in the same pass.
 * Resolve these exact, binary-gated fields once, and touch them only on invalidation. */
public final class MergedCacheFields {
    private MergedCacheFields() {}

    private static Field field(Class<?> owner, String name) {
        try {
            var field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Missing gated optimizer cache " + name, exception);
        }
    }

    private static final class Furnace {
        static final Field RECIPE = field(AbstractFurnaceBlockEntity.class, "curRecipe");
        static final Field MISS = field(AbstractFurnaceBlockEntity.class, "failedMatch");
    }

    private static final class Fan {
        static final Field TYPES = field(com.simibubi.create.content.kinetics.fan.AirCurrent.class, "stellar$cachedTypes");
    }

    public static void clearFurnace(Object furnace) {
        try {
            Furnace.RECIPE.set(furnace, null);
            Furnace.MISS.set(furnace, ItemStack.EMPTY);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static void clearFan(Object fan) {
        try {
            var types = (java.util.Map<?, ?>) Fan.TYPES.get(fan);
            if (types != null) types.clear();
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
