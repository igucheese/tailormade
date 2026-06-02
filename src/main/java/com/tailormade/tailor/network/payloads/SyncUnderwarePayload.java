package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.renderer.SkinLayerRenderLayer;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.data.SkinDataClientCache;
import com.tailormade.tailor.data.UnderwearDataClientCache;
import com.tailormade.tailor.data.UnderwearSetting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncUnderwarePayload (UUID uuid, UnderwearSetting setting) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "sync_underwear");
    public static final Type<SyncUnderwarePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncUnderwarePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeUUID(p.uuid()); UnderwearSetting.STREAM_CODEC.encode(buf, p.setting()); },
                    buf -> new SyncUnderwarePayload(buf.readUUID(), UnderwearSetting.STREAM_CODEC.decode(buf))
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncUnderwarePayload packet, IPayloadContext ctx) {
//        ctx.enqueueWork(() ->
//                SkinLayerRenderLayer.updateUnderwear(packet.uuid(), packet.setting()));
        ctx.enqueueWork(() -> {
            UnderwearDataClientCache.updateCache(packet.uuid(), packet.setting());
            Tailormade.LOGGER.info("[CACHE_SYNC_UNDERWEAR] Sync completed: " + packet.uuid());
        });
    }
}
