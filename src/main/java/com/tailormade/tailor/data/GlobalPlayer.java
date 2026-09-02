package com.tailormade.tailor.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record GlobalPlayer(
        UUID uuid,
        String name
) {
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("uuid", uuid);
        nbt.putString("name", name);
        return nbt;
    }

    public static GlobalPlayer load(CompoundTag nbt) {
        UUID uuid = nbt.getUUID("uuid");
        String name = nbt.getString("name");
        return new GlobalPlayer(uuid, name);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, GlobalPlayer> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUUID(info.uuid());
                buf.writeUtf(info.name());
            },
            buf -> {
                return new GlobalPlayer(
                        buf.readUUID(),
                        buf.readUtf()
                );
            }
    );
}
