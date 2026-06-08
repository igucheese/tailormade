package com.tailormade.tailor.data;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;

public record PixelData(int[] pixels) {
    public static final Codec<PixelData> CODEC =
            Codec.INT_STREAM.xmap(
                    stream -> new PixelData(stream.toArray()),
                    data   -> Arrays.stream(data.pixels)
            );

    public static final StreamCodec<FriendlyByteBuf, PixelData> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        buf.writeVarInt(data.pixels.length);
                        for (int pixel : data.pixels) buf.writeInt(pixel);
                    },
                    buf -> {
                        int len = buf.readVarInt();
                        int[] arr = new int[len];
                        for (int i = 0; i < len; i++) arr[i] = buf.readInt();
                        return new PixelData(arr);
                    }
            );

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PixelData other)) return false;
        return Arrays.equals(this.pixels, other.pixels);
    }

    public int[] getPixels() { return pixels; }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pixels);
    }

    @Override
    public String toString() {
        return "PixelData[length=" + pixels.length + "]";
    }
}
