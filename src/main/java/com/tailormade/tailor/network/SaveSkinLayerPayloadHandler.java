package com.tailormade.tailor.network;

import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.data.PowderRoomSavedData;
import com.tailormade.tailor.network.payloads.SaveSkinLayerPayload;
import com.tailormade.tailor.network.payloads.SyncDesignPayload;
import com.tailormade.tailor.network.payloads.SyncSkinLayerPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SaveSkinLayerPayloadHandler {
    public static void handle(SaveSkinLayerPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (packet.pixels().length != 64 * 64) return;

            ServerLevel overworld = player.getServer().overworld();
            PowderRoomSavedData.get(overworld).setSkinLayer(player.getUUID(), new PixelData(packet.pixels()));

            SyncSkinLayerPayload syncPacket =
                    new SyncSkinLayerPayload(player.getUUID(), packet.pixels());
            PacketDistributor.sendToAllPlayers(syncPacket);
        });
    }
}
