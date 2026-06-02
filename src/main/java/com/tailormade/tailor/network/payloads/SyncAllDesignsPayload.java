package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncAllDesignsPayload(Collection<DesignDataRecord> designs) implements CustomPacketPayload {
    public static final Type<SyncAllDesignsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "design_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAllDesignsPayload> STREAM_CODEC = StreamCodec.composite(
            DesignDataRecord.STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)), SyncAllDesignsPayload::designs,
            SyncAllDesignsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAllDesignsPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            for (DesignDataRecord design: payload.designs()) {
                DesignDataClientCache.updateCache(design.uuid(), design);
                Tailormade.LOGGER.info("[CACHE_SYNC_DESIGN] Sync completed: " + design.uuid());
            }
        });
    }
}