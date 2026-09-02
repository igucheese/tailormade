package com.tailormade.tailor.utils.export;

import com.tailormade.tailor.data.DesignDataRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;

public class DesignDataNbtExporter {
    private DesignDataNbtExporter() {}
    public static CompoundTag toNbt(DesignDataRecord record) {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", record.uuid());
        tag.put("pixelData", new IntArrayTag(record.pixelData().getPixels()));
        tag.putUUID("userId", record.userId());
        tag.putString("name", record.name());
        tag.putString("type", record.type());
        tag.putBoolean("isLocked", record.isLocked());
        return tag;
    }
}
