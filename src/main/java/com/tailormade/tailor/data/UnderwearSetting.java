package com.tailormade.tailor.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

public record UnderwearSetting(UnderwearType type, DyeColor color) {
    public static final UnderwearSetting DEFAULT =
            new UnderwearSetting(UnderwearType.MALE_BOXER, DyeColor.BLACK);

    public static final Codec<UnderwearSetting> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.xmap(
                    s -> UnderwearType.valueOf(s.toUpperCase()),
                    UnderwearType::name
            ).fieldOf("type").forGetter(UnderwearSetting::type),
            Codec.STRING.xmap(
                    s -> DyeColor.byName(s, DyeColor.WHITE),
                    DyeColor::getName
            ).fieldOf("color").forGetter(UnderwearSetting::color)
    ).apply(inst, UnderwearSetting::new));

    public static final StreamCodec<FriendlyByteBuf, UnderwearSetting> STREAM_CODEC =
            StreamCodec.of(
                    (buf, s) -> {
                        buf.writeEnum(s.type());
                        buf.writeEnum(s.color());
                    },
                    buf -> new UnderwearSetting(
                            buf.readEnum(UnderwearType.class),
                            buf.readEnum(DyeColor.class)
                    )
            );
}
