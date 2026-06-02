package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.renderer.SkinLayerRenderLayer;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.data.SkinDataClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncSkinLayerPayload (UUID uuid, int[] pixels) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "sync_skin_layer");
    public static final Type<SyncSkinLayerPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncSkinLayerPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeUUID(p.uuid());
                        buf.writeVarInt(p.pixels().length);
                        for (int px : p.pixels()) buf.writeInt(px);
                    },
                    buf -> {
                        UUID uuid = buf.readUUID();
                        int len   = buf.readVarInt();
                        int[] arr = new int[len];
                        for (int i = 0; i < len; i++) arr[i] = buf.readInt();
                        return new SyncSkinLayerPayload(uuid, arr);
                    }
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncSkinLayerPayload packet, IPayloadContext ctx) {
//        ctx.enqueueWork(() ->
//                SkinLayerRenderLayer.updateSkinPixels(packet.uuid(), packet.pixels()));
        ctx.enqueueWork(() -> {
            SkinDataClientCache.updateCache(packet.uuid(), new PixelData(packet.pixels()));
            Tailormade.LOGGER.info("[CACHE_SYNC_SKIN] Sync completed: " + packet.uuid());
        });
    }
}