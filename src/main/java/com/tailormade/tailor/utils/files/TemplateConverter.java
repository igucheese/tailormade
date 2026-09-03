package com.tailormade.tailor.utils.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;

public final class TemplateConverter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private TemplateConverter() {}

    public static String toTemplateJson(CompoundTag exportedNbt) {
        JsonObject json = new JsonObject();
        json.addProperty("name", exportedNbt.getString("name"));
        json.addProperty("type", exportedNbt.getString("type"));

        int[] pixels = exportedNbt.getIntArray("pixelData");
        var pixelArray = new com.google.gson.JsonArray();
        for (int pixel : pixels) {
            pixelArray.add(pixel);
        }
        json.add("pixelData", pixelArray);

        return GSON.toJson(json);
    }
}
