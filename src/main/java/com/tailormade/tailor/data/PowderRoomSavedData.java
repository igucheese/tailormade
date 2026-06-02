package com.tailormade.tailor.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PowderRoomSavedData extends SavedData {
    private static final String NAME = "tailormade_powder_room";
    private final Map<UUID, PixelData> skinLayers = new HashMap<>();
    public static PowderRoomSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(
                                PowderRoomSavedData::new,
                                PowderRoomSavedData::load
                        ),
                        NAME
                );
    }

    public void setSkinLayer(UUID uuid, PixelData data) {
        skinLayers.put(uuid, data);
        setDirty();
    }

    public PixelData getSkinLayer(UUID uuid) {
        return skinLayers.get(uuid);
    }
    public Collection<PixelData> index() {
        return skinLayers.values();
    }

    public void removeSkinLayer(UUID uuid) {
        skinLayers.remove(uuid);
        setDirty();
    }

    private static PowderRoomSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PowderRoomSavedData data = new PowderRoomSavedData();
        CompoundTag players = tag.getCompound("PowderRoom");
        for (String key : players.getAllKeys()) {
            UUID uuid     = UUID.fromString(key);
            int[] pixels  = players.getIntArray(key);
            data.skinLayers.put(uuid, new PixelData(pixels));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        skinLayers.forEach((uuid, pd) ->
                players.put(uuid.toString(), new IntArrayTag(pd.pixels())));
        tag.put("PowderRoom", players);
        return tag;
    }
}
