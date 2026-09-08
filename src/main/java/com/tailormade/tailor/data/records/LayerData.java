package com.tailormade.tailor.data.records;

import com.tailormade.tailor.data.PixelData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record LayerData(
        int id,
        int layerIndex,
        boolean isVisible,
        PixelData pixelData,
        int alpha
) {
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("id", this.id);
        nbt.putInt("layerIndex", this.layerIndex);
        nbt.putBoolean("isVisible", this.isVisible);
        nbt.putIntArray("pixels", this.pixelData.pixels());
        nbt.putInt("alpha", this.alpha);
        return nbt;
    }

    public static LayerData load(CompoundTag nbt) {
        int id = nbt.getInt("id");
        int layerIndex = nbt.getInt("layerIndex");
        boolean isVisible = nbt.getBoolean("isVisible");
        int[] pixels = nbt.getIntArray("pixels");
        int alpha = nbt.getInt("alpha");

        return new LayerData(id, layerIndex, isVisible, new PixelData(pixels), alpha);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, LayerData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.id());
                buf.writeVarInt(data.layerIndex());
                buf.writeBoolean(data.isVisible());
                PixelData.STREAM_CODEC.encode(buf, data.pixelData());
                buf.writeVarInt(data.alpha());
            },
            buf -> new LayerData(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    PixelData.STREAM_CODEC.decode(buf),
                    buf.readVarInt()
            )
    );
}
