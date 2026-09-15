package com.tailormade.tailor.utils.files;

import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.records.LayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;

public class DesignDataNbtExporter {
    private DesignDataNbtExporter() {}
    public static CompoundTag toNbt(DesignDataRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", record.uuid());
        tag.put("pixelData", new IntArrayTag(record.pixelData().getPixels()));

        ListTag layersTag = new ListTag();
        for (LayerData layer : record.layers()) {
            layersTag.add(layer.save());
        }
        tag.put("layers", layersTag);

        tag.putUUID("userId", record.userId());
        tag.putString("name", record.name());
        tag.putString("type", record.type());
        tag.putBoolean("isLocked", record.isLocked());
        tag.putUUID("designerId", record.designerId());
        return tag;
    }
}
