package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.data.PixelData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public record ConfirmBleachPayload() implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "confirm_bleaching");

    public static final Type<ConfirmBleachPayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ConfirmBleachPayload> STREAM_CODEC =
            StreamCodec.of(
                    ConfirmBleachPayload::encode,
                    ConfirmBleachPayload::decode
            );

    private static void encode(FriendlyByteBuf buf, ConfirmBleachPayload packet) {}

    private static ConfirmBleachPayload decode(FriendlyByteBuf buf) {
        return new ConfirmBleachPayload();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}