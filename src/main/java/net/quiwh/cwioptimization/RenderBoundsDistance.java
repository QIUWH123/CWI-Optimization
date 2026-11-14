package net.quiwh.cwioptimization;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RenderBoundsDistance {
    private RenderBoundsDistance() {}

    /** Same horizontal radius and shifted vertical band as Chloride, applied to rendered geometry. */
    public static boolean inRange(AABB bounds, Vec3 camera, int maxHeight, int maxDistanceSquared) {
        double x = Math.max(bounds.minX - camera.x, Math.max(0, camera.x - bounds.maxX));
        double y = Math.max(bounds.minY - camera.y - 4, Math.max(0, camera.y + 4 - bounds.maxY));
        double z = Math.max(bounds.minZ - camera.z, Math.max(0, camera.z - bounds.maxZ));
        return y < maxHeight && x * x + z * z < maxDistanceSquared;
    }
}
