package com.tailormade.tailor.data.records;

import com.tailormade.tailor.data.GlobalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record DesignTemplate(String name, String type, int[] pixelData) {
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", name);
        nbt.putString("type", type);
        nbt.put("pixelData", new IntArrayTag(pixelData));
        return nbt;
    }

    public static DesignTemplate load(CompoundTag nbt) {
        String name = nbt.getString("name");
        String type = nbt.getString("type");
        int[] pixels = nbt.getIntArray("pixelData");
        return new DesignTemplate(name, type, pixels);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, DesignTemplate> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUtf(info.name());
                buf.writeUtf(info.type());
                buf.writeVarIntArray(info.pixelData());
            },
            buf -> {
                return new DesignTemplate(
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readVarIntArray()
                );
            }
    );
}
