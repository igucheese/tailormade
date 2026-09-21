package com.tailormade.tailor.data.records;

import com.tailormade.tailor.data.DesignDataRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CatalogEnlistData(
        DesignDataRecord design,
        boolean isListed
) {
    public CatalogEnlistData withListed() {
        return new CatalogEnlistData(design, true);
    }
    public CatalogEnlistData withUnlisted() {
        return new CatalogEnlistData(design, false);
    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("design", design.save());
        nbt.putBoolean("isListed", isListed);
        return nbt;
    }

    public static CatalogEnlistData load(CompoundTag nbt) {
        DesignDataRecord design = DesignDataRecord.load(nbt.getCompound("design"));
        boolean isListed = nbt.getBoolean("isListed");
        return new CatalogEnlistData(design, isListed);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CatalogEnlistData> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                DesignDataRecord.STREAM_CODEC.encode(buf, info.design());
                buf.writeBoolean(info.isListed());
            },
            buf -> {
                return new CatalogEnlistData(
                        DesignDataRecord.STREAM_CODEC.decode(buf),
                        buf.readBoolean()
                );
            }
    );
}
