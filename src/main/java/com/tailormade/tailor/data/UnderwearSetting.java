package com.tailormade.tailor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

import java.util.UUID;

public record UnderwearSetting(UnderwearType type, int color) {
    public static final UnderwearSetting DEFAULT =
            new UnderwearSetting(UnderwearType.MALE_BOXER, 0xFF000000);

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", type.getTextureKey());
        nbt.putInt("color", color);
        return nbt;
    }

    public static UnderwearSetting load(CompoundTag nbt) {
        String typeName = nbt.getString("type");
        int color = nbt.getInt("color");
        return new UnderwearSetting(UnderwearType.getType(typeName), color);
    }

    public static final StreamCodec<FriendlyByteBuf, UnderwearSetting> STREAM_CODEC =
            StreamCodec.of(
                    (buf, s) -> {
                        buf.writeEnum(s.type());
                        buf.writeInt(s.color());
                    },
                    buf -> new UnderwearSetting(
                            buf.readEnum(UnderwearType.class),
                            buf.readInt()
                    )
            );
}
