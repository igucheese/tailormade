package com.tailormade.tailor.utils.files;

import com.tailormade.tailor.data.DesignDataRecord;
import com.tailormade.tailor.data.PixelData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.UUID;

public final class DesignDataNbtImporter {
    private DesignDataNbtImporter() {}

    public static DesignDataRecord fromNbt(CompoundTag tag) {
        if (!tag.hasUUID("uuid")) {
            throw new IllegalArgumentException("Missing or invalid 'uuid' field");
        }
        if (!tag.contains("pixelData", Tag.TAG_INT_ARRAY)) {
            throw new IllegalArgumentException("Missing or invalid 'pixelData' field");
        }
        if (!tag.hasUUID("userId")) {
            throw new IllegalArgumentException("Missing or invalid 'userId' field");
        }
        if (!tag.contains("name", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Missing or invalid 'name' field");
        }
        if (!tag.contains("type", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Missing or invalid 'type' field");
        }
        if (!tag.contains("isLocked", Tag.TAG_BYTE)) {
            throw new IllegalArgumentException("Missing or invalid 'isLocked' field");
        }
        if (!tag.hasUUID("designerId")) {
            throw new IllegalArgumentException("Missing or invalid 'designerId' field");
        }

        UUID uuid = tag.getUUID("uuid");
        int[] pixels = tag.getIntArray("pixelData");
        UUID userId = tag.getUUID("userId");
        String name = tag.getString("name");
        String type = tag.getString("type");
        boolean isLocked = tag.getBoolean("isLocked");
        UUID designerId = tag.getUUID("designerId");

        PixelData pixelData = new PixelData(pixels);

        return new DesignDataRecord(uuid, pixelData, userId, name, type, isLocked, designerId);
    }
}
