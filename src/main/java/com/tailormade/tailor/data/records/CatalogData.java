package com.tailormade.tailor.data.records;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CatalogData(
        UUID id,
        UUID playerId,
        String name,
        List<UUID> patterns
) {
    public CatalogData withName(String newName) {
        return new CatalogData(id, playerId, newName, patterns);
    }
    public CatalogData withPatterns(List<UUID> newPatterns) {
        return new CatalogData(id, playerId, name, newPatterns);
    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("id", id);
        nbt.putUUID("playerId", playerId);
        nbt.putString("name", name);

        ListTag patternsList = new ListTag();
        patterns.forEach(id -> patternsList.add(StringTag.valueOf(id.toString())));
        nbt.put("patterns", patternsList);

        return nbt;
    }

    public static CatalogData load(CompoundTag nbt) {
        UUID id = nbt.getUUID("id");
        UUID playerId = nbt.getUUID("playerId");
        String name = nbt.getString("name");

        List<UUID> patterns = new ArrayList<>();
        ListTag patternsList = nbt.getList("patterns", Tag.TAG_STRING);
        for (int i = 0; i < patternsList.size(); i++) {
            patterns.add(UUID.fromString(patternsList.getString(i)));
        }

        return new CatalogData(id, playerId, name, patterns);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, CatalogData> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUUID(info.id());
                buf.writeUUID(info.playerId());
                buf.writeUtf(info.name());
                buf.writeCollection(info.patterns(), RegistryFriendlyByteBuf::writeUUID);
            },
            buf -> {
                return new CatalogData(
                        buf.readUUID(),
                        buf.readUUID(),
                        buf.readUtf(),
                        buf.readCollection(ArrayList::new, RegistryFriendlyByteBuf::readUUID)
                );
            }
    );
}
