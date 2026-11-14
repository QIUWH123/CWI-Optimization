package net.quiwh.cwioptimization;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamProperties;
import net.lpcamors.optical.blocks.optical_source.BeamHelper.BeamSignal;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity.BeamSection;

/** Immutable copy of the packet's observable state, including mutable signal messages. */
public record OpticalSnapshot(List<BeamSection> sections, Map<BlockPos, BeamProperties> activators) {
    public static OpticalSnapshot capture(List<BeamSection> sections, Map<BlockPos, BeamProperties> activators) {
        return new OpticalSnapshot(sections.stream().map(section -> new BeamSection(
                position(section.fromPos()), position(section.toPos()), freeze(section.beamProperties()))).toList(),
                activators.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                        entry -> entry.getKey().immutable(), entry -> freeze(entry.getValue()))));
    }

    private static Vec3i position(Vec3i pos) {
        return new Vec3i(pos.getX(), pos.getY(), pos.getZ());
    }

    private static BeamProperties freeze(BeamProperties properties) {
        return new BeamProperties(properties.intensity(), properties.direction(), properties.beamType(),
                properties.polarization(), position(properties.color()), properties.signal().stream()
                .map(signal -> new BeamSignal(signal.freq(), List.copyOf(signal.message()))).toList(),
                properties.spin(), properties.forceVisibility(), properties.forcePenetration(), properties.isDirty());
    }
}
