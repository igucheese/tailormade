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

public class DesignData extends SavedData {
    private static final String NAME = "tailormade_design_data_registry";
    private final Map<UUID, DesignDataRecord> designData = new HashMap<>();

    public static DesignData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(
                        DesignData::new,
                        DesignData::load,
                        DataFixTypes.LEVEL
                ),
                NAME
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        System.out.println("[Tailormade][DESIGN_DATA] Saving design data to file...");
        ListTag listTag = new ListTag();
        for (DesignDataRecord info : designData.values()) {
            listTag.add(info.save());
        }
        tag.put("DesignedData", listTag);
        System.out.println("[Tailormade][DESIGN_DATA] saving designs; targets: " + this.designData.size());
        return tag;
    }

    public DesignData() {}
    public static DesignData load(CompoundTag tag, HolderLookup.Provider registries) {
        System.out.println("[Tailormade][DESIGN_DATA] loading data from file...");
        DesignData data = new DesignData();
        ListTag listTag = tag.getList("DesignedData", Tag.TAG_COMPOUND);

        for (int i = 0; i < listTag.size(); i++) {
            DesignDataRecord info = DesignDataRecord.load(listTag.getCompound(i));
            data.designData.put(info.uuid(), info);
        }
        System.out.println("[Tailormade][DESIGN_DATA] LoadData Loaded: " + data.designData.size() + " items");
        return data;
    }

    public void addDesign(DesignDataRecord design) {
        designData.put(design.uuid(), design);
        this.setDirty();
    }
    public void updateDesign(UUID id, DesignDataRecord design) {
        designData.put(id, design);
        this.setDirty();
    }
    public void deleteDesign(UUID id) {
        designData.remove(id);
        this.setDirty();
    }
    public DesignDataRecord get(UUID id) {
        return designData.get(id);
    }

    public Collection<DesignDataRecord> index() {
        return designData.values();
    }
    public Collection<DesignDataRecord> getByName(String name) {
        return designData.values().stream().filter(d -> d.name().contains(name)).toList();
    }
    public Collection<DesignDataRecord> getByType(String typeName) {
        return designData.values().stream().filter(d -> d.type().equals(typeName)).toList();
    }
    public Collection<DesignDataRecord> getByUser(UUID userId) {
        return designData.values().stream().filter(d -> d.userId().equals(userId)).toList();
    }
}

