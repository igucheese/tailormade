package com.tailormade.tailor.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WardrobeSavedData extends SavedData {

    private static final String NAME = "tailormade_wardrobe";

    private final Map<UUID, UnderwearRecord> settings = new HashMap<>();

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
        UnderwearRecord record = new UnderwearRecord(uuid, setting);
        settings.put(uuid, record);
        this.setDirty();
    }

    public UnderwearSetting getSetting(UUID uuid) {
        UnderwearRecord record = settings.get(uuid);
        if (record != null) {
            return record.settings();
        } else {
            return UnderwearSetting.DEFAULT;
        }
    }
    public Collection<UnderwearSetting> index() {
        return settings.values().stream().map(UnderwearRecord::settings).toList();
    }

    public WardrobeSavedData() {}
    public static WardrobeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        System.out.println("[Tailormade][UNDERWEAR_DATA] loading data from file...");
        WardrobeSavedData data = new WardrobeSavedData();
        ListTag listTag = tag.getList("PlayersWardrobe", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            UnderwearRecord uwData = UnderwearRecord.load(listTag.getCompound(i));
            data.settings.put(uwData.uuid(), uwData);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        System.out.println("[Tailormade][UNDERWEAR_DATA] Saving design data to file...");
        ListTag listTag = new ListTag();
        for (UnderwearRecord info : settings.values()) {
            listTag.add(info.save());
        }
        tag.put("PlayersWardrobe", listTag);
        System.out.println("[Tailormade][UNDERWEAR_DATA] saving underwears; targets: " + this.settings.size());
        return tag;
    }

    public record UnderwearRecord(
            UUID uuid,
            UnderwearSetting settings
    ) {
        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("uuid", uuid);
            nbt.putString("type", settings.type().name());
            nbt.putInt("color", settings.color());
            return nbt;
        }

        public static UnderwearRecord load(CompoundTag nbt) {
            UUID uuid = nbt.getUUID("uuid");
            String type = nbt.getString("type");
            int color = nbt.getInt("color");
            UnderwearType uwType  = UnderwearType.getType(type);
            UnderwearSetting setting = new UnderwearSetting(uwType, color);
            return new UnderwearRecord(uuid, setting);
        }

        public static final StreamCodec<RegistryFriendlyByteBuf, UnderwearRecord> STREAM_CODEC = StreamCodec.of(
                (buf, info) -> {
                    buf.writeUUID(info.uuid());
                    UnderwearSetting.STREAM_CODEC.encode(buf, info.settings());
                },
                buf -> {
                    return new UnderwearRecord(
                            buf.readUUID(),
                            UnderwearSetting.STREAM_CODEC.decode(buf)
                    );
                }
        );
    }
}
