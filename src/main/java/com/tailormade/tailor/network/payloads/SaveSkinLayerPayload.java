package com.tailormade.tailor.network.payloads;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public record SaveSkinLayerPayload(int[] pixels) implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "save_skin_layer");
    public static final Type<SaveSkinLayerPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SaveSkinLayerPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> { buf.writeVarInt(p.pixels().length); for (int px : p.pixels()) buf.writeInt(px); },
                    buf -> { int len = buf.readVarInt(); int[] arr = new int[len]; for (int i = 0; i < len; i++) arr[i] = buf.readInt(); return new SaveSkinLayerPayload(arr); }
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}