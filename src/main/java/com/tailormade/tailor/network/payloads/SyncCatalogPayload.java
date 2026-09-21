package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.menu.DesignerMenu;
import com.tailormade.tailor.data.CatalogDataClientCache;
import com.tailormade.tailor.data.DesignDataClientCache;
import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.records.CatalogData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncCatalogPayload(CatalogData catalog) implements CustomPacketPayload {
    public static final Type<SyncCatalogPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCatalogPayload> STREAM_CODEC = StreamCodec.composite(
            CatalogData.STREAM_CODEC, SyncCatalogPayload::catalog,
            SyncCatalogPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncCatalogPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CatalogDataClientCache.updateCache(payload.catalog().id(), payload.catalog());
            Tailormade.LOGGER.info("[CACHE_SYNC_CATALOG] Sync completed: " + payload.catalog().id());
        });
    }
}