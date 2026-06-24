package com.tailormade.tailor.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PowderRoomSavedData extends SavedData {
    private static final String NAME = "tailormade_powder_room";
    private final Map<UUID, SkinDataRecord> skinLayers = new HashMap<>();
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
        skinLayers.put(uuid, new SkinDataRecord(uuid, data));
        setDirty();
    }

    public PixelData getSkinLayer(UUID uuid) {
        SkinDataRecord data = skinLayers.get(uuid);
        return data != null ? data.pixelData() : null;
    }
    public Collection<PixelData> index() {
        return skinLayers.values().stream().map(SkinDataRecord::pixelData).toList();
    }

    public void removeSkinLayer(UUID uuid) {
        skinLayers.remove(uuid);
        setDirty();
    }

    public PowderRoomSavedData() {}
    public static PowderRoomSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        System.out.println("[Tailormade][SKIN_DATA] loading data from file...");
        PowderRoomSavedData data = new PowderRoomSavedData();
        ListTag listTag = tag.getList("PowderRoom", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            SkinDataRecord skinData = SkinDataRecord.load(listTag.getCompound(i));
            data.skinLayers.put(skinData.uuid(), skinData);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        System.out.println("[Tailormade][SKIN_DATA] Saving design data to file...");
        ListTag listTag = new ListTag();
        for (SkinDataRecord info : skinLayers.values()) {
            listTag.add(info.save());
        }
        tag.put("PowderRoom", listTag);
        System.out.println("[Tailormade][SKIN_DATA] saving skins; targets: " + this.skinLayers.size());
        return tag;
    }

    public record SkinDataRecord(
            UUID uuid,
            PixelData pixelData
    ) {
        public CompoundTag save() {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("uuid", uuid);
            nbt.put("pixelData", new IntArrayTag(pixelData.getPixels()));
            return nbt;
        }

        public static SkinDataRecord load(CompoundTag nbt) {
            UUID uuid = nbt.getUUID("uuid");
            int[] pixels = nbt.getIntArray("pixelData");
            PixelData pixelData = new PixelData(pixels);

            return new SkinDataRecord(uuid, pixelData);
        }

        public static final StreamCodec<RegistryFriendlyByteBuf, SkinDataRecord> STREAM_CODEC = StreamCodec.of(
                (buf, info) -> {
                    buf.writeUUID(info.uuid());
                    PixelData.STREAM_CODEC.encode(buf, info.pixelData());
                },
                buf -> {
                    return new SkinDataRecord(
                            buf.readUUID(),
                            PixelData.STREAM_CODEC.decode(buf)
                    );
                }
        );
    }
}
