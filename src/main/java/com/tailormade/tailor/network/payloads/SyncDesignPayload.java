package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.renderer.SkinLayerRenderLayer;
import com.tailormade.tailor.data.DesignData;
import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PixelData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncDesignPayload(UUID uuid, DesignDataRecord design) implements CustomPacketPayload {
    public static final Type<SyncDesignPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "design_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDesignPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SyncDesignPayload::uuid,
            DesignDataRecord.STREAM_CODEC, SyncDesignPayload::design,
            SyncDesignPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDesignPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            DesignDataClientCache.updateCache(payload.uuid(), payload.design());
            Tailormade.LOGGER.info("[CACHE_SYNC_DESIGN] Sync completed: " + payload.uuid());
        });
    }
}