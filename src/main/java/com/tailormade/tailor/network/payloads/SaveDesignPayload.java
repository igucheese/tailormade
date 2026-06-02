package com.tailormade.tailor.network.payloads;

import com.tailormade.tailor.data.PixelData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static com.tailormade.tailor.Tailormade.MODID;

public record SaveDesignPayload (int slotIndex, PixelData pixelData, String name) implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MODID, "save_design_pattern");

    public static final CustomPacketPayload.Type<SaveDesignPayload> TYPE =
            new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, SaveDesignPayload> STREAM_CODEC =
            StreamCodec.of(
                    SaveDesignPayload::encode,
                    SaveDesignPayload::decode
            );

    private static void encode(FriendlyByteBuf buf, SaveDesignPayload packet) {
        buf.writeVarInt(packet.slotIndex());
        PixelData.STREAM_CODEC.encode(buf, packet.pixelData());
        buf.writeUtf(packet.name());
    }

    private static SaveDesignPayload decode(FriendlyByteBuf buf) {
        int slotIndex = buf.readVarInt();
        PixelData pixels = PixelData.STREAM_CODEC.decode(buf);
        String name = buf.readUtf();
        return new SaveDesignPayload(slotIndex, pixels, name);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}