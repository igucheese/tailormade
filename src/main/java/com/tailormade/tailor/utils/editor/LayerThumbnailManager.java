package com.tailormade.tailor.utils.editor;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LayerThumbnailManager {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final int width;
    private final int height;
    private final Map<Integer, DynamicTexture> textures = new HashMap<>();
    private final Map<Integer, ResourceLocation> locations = new HashMap<>();

    public LayerThumbnailManager(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void update(int layerId, int[] layerPixels) {
        DynamicTexture tex = textures.get(layerId);
        if (tex == null) {
            tex = new DynamicTexture(width, height, true);
            ResourceLocation loc = Minecraft.getInstance()
                    .getTextureManager()
                    .register("tailor_layer_thumb_" + layerId + "_" + COUNTER.getAndIncrement(), tex);
            textures.put(layerId, tex);
            locations.put(layerId, loc);
        }

        NativeImage img = tex.getPixels();
        if (img == null) return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = layerPixels[y * width + x];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >>  8) & 0xFF;
                int b =  argb & 0xFF;
                img.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        tex.upload();
    }

    public ResourceLocation getLocation(int layerId) {
        return locations.get(layerId);
    }

    public void remove(int layerId) {
        DynamicTexture tex = textures.remove(layerId);
        if (tex != null) tex.close();
        locations.remove(layerId);
    }

    public void closeAll() {
        textures.values().forEach(DynamicTexture::close);
        textures.clear();
        locations.clear();
    }
}
