package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.data.DesignTemplateCache;
import com.tailormade.tailor.data.GlobalPlayer;
import com.tailormade.tailor.data.GlobalPlayerCache;
import com.tailormade.tailor.data.records.DesignTemplate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.tailormade.tailor.Tailormade.MODID;

public record SyncDesignTemplatePayload(DesignTemplate template) implements CustomPacketPayload {
    public static final Type<SyncDesignTemplatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "design_template_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDesignTemplatePayload> STREAM_CODEC = StreamCodec.composite(
            DesignTemplate.STREAM_CODEC, SyncDesignTemplatePayload::template,
            SyncDesignTemplatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDesignTemplatePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            DesignTemplateCache.addCache(payload.template());
            Tailormade.LOGGER.info("[CACHE_SYNC_DESIGN_TEMPLATE] Sync completed: " + payload.template());
        });
    }
}