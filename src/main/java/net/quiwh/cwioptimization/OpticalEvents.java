package net.quiwh.cwioptimization;

import net.createmod.catnip.platform.CatnipServices;
import net.lpcamors.optical.blocks.optical_source.GenericOpticalSourceBlockEntity;
import net.lpcamors.optical.network.ConfigureOpticalSourcePacket;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

/** Loaded only after the optional Optical compatibility gate succeeds. */
final class OpticalEvents {
    private OpticalEvents() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(OpticalEvents::chunkSent);
    }

    static void chunkSent(ChunkWatchEvent.Sent event) {
        // Sent, rather than Watch: the client must receive the chunk before its beams.
        for (var entity : event.getChunk().getBlockEntities().values()) {
            if (entity instanceof GenericOpticalSourceBlockEntity source && !source.isRemoved()) {
                CatnipServices.NETWORK.sendToClient(event.getPlayer(), new ConfigureOpticalSourcePacket(source));
            }
        }
    }
}
