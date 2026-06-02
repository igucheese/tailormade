package com.tailormade.tailor.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public record ConfirmTailorPayload(String name, String serial, String tailorName) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "confirm_tailor");

    public static final CustomPacketPayload.Type<ConfirmTailorPayload> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ConfirmTailorPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfirmTailorPayload::name,
            ByteBufCodecs.STRING_UTF8, ConfirmTailorPayload::serial,
            ByteBufCodecs.STRING_UTF8, ConfirmTailorPayload::tailorName,
            ConfirmTailorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}