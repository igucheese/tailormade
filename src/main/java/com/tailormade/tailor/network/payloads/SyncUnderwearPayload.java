package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.UnderwearDataClientCache;
import com.tailormade.tailor.data.UnderwearSetting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncUnderwearPayload(UUID uuid, UnderwearSetting setting) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "sync_underwear");
    public static final Type<SyncUnderwearPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SyncUnderwearPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SyncUnderwearPayload::uuid,
            UnderwearSetting.STREAM_CODEC, SyncUnderwearPayload::setting,
            SyncUnderwearPayload::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncUnderwearPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            UnderwearDataClientCache.updateCache(packet.uuid(), packet.setting());
            Tailormade.LOGGER.info("[CACHE_SYNC_UNDERWEAR] Sync completed: " + packet.uuid() + " body: " + packet.setting());
        });
    }
}
