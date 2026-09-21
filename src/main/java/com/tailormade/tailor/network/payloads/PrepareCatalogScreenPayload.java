package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.data.CatalogSavedData;
import com.tailormade.tailor.data.records.CatalogData;
import com.tailormade.tailor.utils.CatalogService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.UUID;

import static com.tailormade.tailor.Tailormade.MODID;

public record PrepareCatalogScreenPayload(UUID id, boolean isFromLectern, BlockPos pos) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MODID, "prepare_catalog_screen");

    public static final Type<PrepareCatalogScreenPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PrepareCatalogScreenPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PrepareCatalogScreenPayload::id,
            ByteBufCodecs.BOOL, PrepareCatalogScreenPayload::isFromLectern,
            BlockPos.STREAM_CODEC, PrepareCatalogScreenPayload::pos,
            PrepareCatalogScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PrepareCatalogScreenPayload packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            CatalogService.openClientScreen(player, packet.id(), packet.isFromLectern(), packet.pos());
        });
    }
}