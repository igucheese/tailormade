package com.tailormade.tailor.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.UUID;

public record DesignDataRecord(
        UUID uuid,
        PixelData pixelData,
        UUID userId,
        String name,
        String type,
        boolean isLocked
) {
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("uuid", uuid);
        nbt.put("pixelData", new IntArrayTag(pixelData.getPixels()));
        nbt.putUUID("userId", userId);
        nbt.putString("name", name);
        nbt.putString("type", type);
        nbt.putBoolean("isLocked", isLocked);
        return nbt;
    }

    public static DesignDataRecord load(CompoundTag nbt) {
        UUID uuid = nbt.getUUID("uuid");
        int[] pixels = nbt.getIntArray("pixelData");
        PixelData pixelData = new PixelData(pixels);
        UUID userId = nbt.getUUID("userId");
        String name = nbt.getString("name");
        String type = nbt.getString("type");
        boolean isLocked = nbt.getBoolean("isLocked");

        return new DesignDataRecord(uuid, pixelData, userId, name, type, isLocked);
    }

    public DesignDataRecord withLocked() {
        return new DesignDataRecord(uuid, pixelData, userId, name, type, true);
    }
    public DesignDataRecord withUnlocked() {
        return new DesignDataRecord(uuid, pixelData, userId, name, type, false);
    }
    public DesignDataRecord withName(String newName) {
        return new DesignDataRecord(uuid, pixelData, userId, newName, type, isLocked);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, DesignDataRecord> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUUID(info.uuid());
                PixelData.STREAM_CODEC.encode(buf, info.pixelData());
                buf.writeUUID(info.userId());
                buf.writeUtf(info.name());
                buf.writeUtf(info.type());
                buf.writeBoolean(info.isLocked());
            },
            buf -> {
                return new DesignDataRecord(
                        buf.readUUID(),
                        PixelData.STREAM_CODEC.decode(buf),
                        buf.readUUID(),
                        buf.readUtf(),
                        buf.readUtf(),
                        buf.readBoolean()
                );
            }
    );
}
