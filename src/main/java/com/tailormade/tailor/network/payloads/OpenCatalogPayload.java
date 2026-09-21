package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.Tailormade;
import com.tailormade.tailor.client.ClientHooks;
import com.tailormade.tailor.data.CatalogDataClientCache;
import com.tailormade.tailor.data.records.CatalogData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record OpenCatalogPayload(UUID id, boolean isFromLectern, BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenCatalogPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "open_catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCatalogPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, OpenCatalogPayload::id,
            ByteBufCodecs.BOOL, OpenCatalogPayload::isFromLectern,
            BlockPos.STREAM_CODEC, OpenCatalogPayload::pos,
            OpenCatalogPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCatalogPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientHooks.openCatalogScreen(payload.id(), payload.isFromLectern(), payload.pos());
        });
    }
}