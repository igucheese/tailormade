package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class UnderwearTextureCompositor {
    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private int[] maskPixels;
    private int maskW;
    private int maskH;
    private boolean isPreview = false;

    public UnderwearTextureCompositor() {}

    public void init(ResourceLocation maskResource) {
        try {
            var resourceManager = Minecraft.getInstance().getResourceManager();
            var resource = resourceManager.getResource(maskResource).orElseThrow();

            try (var stream = resource.open()) {
                NativeImage img = NativeImage.read(stream);
                maskW = img.getWidth();
                maskH = img.getHeight();
                maskPixels = new int[maskW * maskH];

                for (int y = 0; y < maskH; y++) {
                    for (int x = 0; x < maskW; x++) {
                        int abgr = img.getPixelRGBA(x, y);
                        int a = (abgr >> 24) & 0xFF;
                        int b = (abgr >> 16) & 0xFF;
                        int g = (abgr >>  8) & 0xFF;
                        int r =  abgr & 0xFF;
                        maskPixels[y * maskW + x] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                }
                img.close();
            }

            dynamicTexture = new DynamicTexture(maskW, maskH, true);
            String textureName = "tailormade_underwear_composite_" + COUNTER.getAndIncrement();
            textureLocation = Minecraft.getInstance()
                    .getTextureManager()
                    .register(textureName, dynamicTexture);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UnderwearTextureCompositor setIsPreview(boolean isPreview) {
        this.isPreview = isPreview;
        return this;
    }

    public ResourceLocation compose(int argbColor, Set<EquipmentSlot> activeSlots) {
        if (dynamicTexture == null || maskPixels == null) return null;
        NativeImage img = dynamicTexture.getPixels();
        if (img == null) return null;

        for (int y = 0; y < maskH; y++) {
            for (int x = 0; x < maskW; x++) {
                int src = maskPixels[y * maskW + x];
                int a = (src >> 24) & 0xFF;

                if (a == 0) { img.setPixelRGBA(x, y, 0); continue; }

                if (!isPreview && !isPixelInActiveSlot(x, y, activeSlots)) {
                    img.setPixelRGBA(x, y, 0);
                    continue;
                }

                int maskBrightness = (src >> 16) & 0xFF;

                float factor;
                if (maskBrightness < 65) factor = 0.85f;
                else if (maskBrightness < 129) factor = 1.0f;
                else if (maskBrightness < 193) factor = 1.15f;
                else factor = 2.5f;

                int r = Math.clamp((int)(((argbColor >> 16) & 0xFF) * factor), 0, 255);
                int g = Math.clamp((int)(((argbColor >>  8) & 0xFF) * factor), 0, 255);
                int b = Math.clamp((int)( (argbColor & 0xFF) * factor), 0, 255);

                img.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }

        dynamicTexture.upload();
        return textureLocation;
    }

    private boolean isPixelInActiveSlot(int x, int y, Set<EquipmentSlot> activeSlots) {
        if (activeSlots.contains(EquipmentSlot.HEAD)) {
            if (x >= 0 && x < 32 && y >= 0 && y < 16) return true;
            if (x >= 32 && x < 64 && y >= 0 && y < 16) return true;
        }
        if (activeSlots.contains(EquipmentSlot.CHEST)) {
            if (x >= 16 && x < 40 && y >= 16 && y < 32) return true;
            if (x >= 16 && x < 40 && y >= 32 && y < 48) return true;
            if (x >= 40 && x < 56 && y >= 16 && y < 32) return true;
            if (x >= 40 && x < 56 && y >= 32 && y < 48) return true;
            if (x >= 32 && x < 48 && y >= 48 && y < 64) return true;
            if (x >= 48 && x < 64 && y >= 48 && y < 64) return true;
        }
        if (activeSlots.contains(EquipmentSlot.LEGS)) {
            if (x >= 0 && x < 16 && y >= 16 && y < 32) return true;
            if (x >= 0 && x < 16 && y >= 32 && y < 48) return true;
            if (x >= 16 && x < 32 && y >= 48 && y < 64) return true;
            if (x >= 0 && x < 16 && y >= 48 && y < 64) return true;
        }
        if (activeSlots.contains(EquipmentSlot.FEET)) {
            if (x >= 0 && x < 16 && y >= 32 && y < 48) return true;
            if (x >= 0 && x < 16 && y >= 48 && y < 64) return true;
        }
        return false;
    }

    public void close() {
        if (dynamicTexture != null) {
            dynamicTexture.close();
            dynamicTexture = null;
        }
    }
}
