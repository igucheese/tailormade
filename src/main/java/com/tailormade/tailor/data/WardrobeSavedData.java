package com.tailormade.tailor.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WardrobeSavedData extends SavedData {

    private static final String NAME = "tailormade_wardrobe";

    private final Map<UUID, UnderwearSetting> settings = new HashMap<>();

    public static WardrobeSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(
                                WardrobeSavedData::new,
                                WardrobeSavedData::load
                        ),
                        NAME
                );
    }

    public void setSetting(UUID uuid, UnderwearSetting setting) {
        settings.put(uuid, setting);
        setDirty();
    }

    public UnderwearSetting getSetting(UUID uuid) {
        return settings.getOrDefault(uuid, UnderwearSetting.DEFAULT);
    }
    public Collection<UnderwearSetting> index() {
        return settings.values();
    }

    private static WardrobeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        WardrobeSavedData data = new WardrobeSavedData();
        CompoundTag players = tag.getCompound("PlayersWardrobe");
        for (String key : players.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            CompoundTag entry = players.getCompound(key);
            UnderwearType type  = UnderwearType.valueOf(entry.getString("Type"));
            DyeColor color = DyeColor.byName(entry.getString("Color"), DyeColor.WHITE);
            data.settings.put(uuid, new UnderwearSetting(type, color));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        settings.forEach((uuid, s) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Type",  s.type().name());
            entry.putString("Color", s.color().getName());
            players.put(uuid.toString(), entry);
        });
        tag.put("PlayersWardrobe", players);
        return tag;
    }
}
