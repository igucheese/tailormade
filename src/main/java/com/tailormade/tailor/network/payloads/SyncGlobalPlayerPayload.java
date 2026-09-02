package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.GlobalPlayer;
import com.tailormade.tailor.data.GlobalPlayerCache;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncGlobalPlayerPayload(GlobalPlayer player) implements CustomPacketPayload {
    public static final Type<SyncGlobalPlayerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "global_player_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncGlobalPlayerPayload> STREAM_CODEC = StreamCodec.composite(
            GlobalPlayer.STREAM_CODEC, SyncGlobalPlayerPayload::player,
            SyncGlobalPlayerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncGlobalPlayerPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            GlobalPlayerCache.updateCache(payload.player().uuid(), payload.player());
            Tailormade.LOGGER.info("[CACHE_SYNC_GLOBAL_PLAYER] Sync completed: " + payload.player());
        });
    }
}