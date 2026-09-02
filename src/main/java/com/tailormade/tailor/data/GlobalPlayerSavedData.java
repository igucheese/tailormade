package com.tailormade.tailor.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalPlayerSavedData extends SavedData {
    private static final String NAME = "tailormade_global_player_registry";
    private final Map<UUID, GlobalPlayer> players = new HashMap<>();

    public static GlobalPlayerSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(
                        GlobalPlayerSavedData::new,
                        GlobalPlayerSavedData::load,
                        DataFixTypes.LEVEL
                ),
                NAME
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        System.out.println("[Tailormade][GLOBAL_PLAYER] Saving player data to file...");
        ListTag listTag = new ListTag();
        for (GlobalPlayer info : players.values()) {
            listTag.add(info.save());
        }
        tag.put("players", listTag);
        System.out.println("[Tailormade][GLOBAL_PLAYER] saving players; targets: " + this.players.size());
        return tag;
    }

    public GlobalPlayerSavedData() {}
    public static GlobalPlayerSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        System.out.println("[Tailormade][GLOBAL_PLAYER] loading data from file...");
        GlobalPlayerSavedData data = new GlobalPlayerSavedData();
        ListTag listTag = tag.getList("players", Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            GlobalPlayer info = GlobalPlayer.load(listTag.getCompound(i));
            data.players.put(info.uuid(), info);
        }
        System.out.println("[Tailormade][GLOBAL_PLAYER] LoadData Loaded: " + data.players.size() + " items");
        return data;
    }

    public void savePlayer(GlobalPlayer player) {
        players.put(player.uuid(), player);
        this.setDirty();
    }
    public void deletePlayer(UUID id) {
        players.remove(id);
        this.setDirty();
    }
    public GlobalPlayer get(UUID id) {
        return players.get(id);
    }
    public Collection<GlobalPlayer> index() {
        return players.values();
    }
}

