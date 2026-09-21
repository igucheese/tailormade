package com.tailormade.tailor.data;

import com.tailormade.tailor.data.records.CatalogData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class CatalogSavedData extends SavedData {
    private static final String NAME = "tailormade_catalog_data_registry";
    private Map<UUID, CatalogData> catalogs = new HashMap<>();

    public static CatalogSavedData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(
                        CatalogSavedData::new,
                        CatalogSavedData::load,
                        DataFixTypes.LEVEL
                ),
                NAME
        );
    }

    public CatalogSavedData() {}
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        System.out.println("[Tailormade][CATALOG_DATA] Saving catalog data to file...");
        ListTag listTag = new ListTag();
        for (CatalogData info : catalogs.values()) {
            listTag.add(info.save());
        }
        tag.put("catalogs", listTag);
        System.out.println("[Tailormade][CATALOG_DATA] saving catalogs; targets: " + this.catalogs.size());
        return tag;
    }
    public static CatalogSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        System.out.println("[Tailormade][CATALOG_DATA] loading data from file...");
        CatalogSavedData data = new CatalogSavedData();
        ListTag listTag = tag.getList("catalogs", Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            CatalogData info = CatalogData.load(listTag.getCompound(i));
            data.catalogs.put(info.id(), info);
        }
        System.out.println("[Tailormade][CATALOG_DATA] LoadData Loaded: " + data.catalogs.size() + " items");
        return data;
    }

    public void saveCatalog(CatalogData catalog) {
        catalogs.put(catalog.id(), catalog);
        this.setDirty();
    }
    public List<CatalogData> getAll() {
        return catalogs.values().stream().toList();
    }
    public CatalogData getCatalog(UUID id) {
        return catalogs.getOrDefault(id, null);
    }
}
