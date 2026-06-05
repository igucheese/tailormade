package com.tailormade.tailor.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.atomic.AtomicInteger;

public class UnderwearTextureCompositor {

    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private int[] maskPixels; // マスク画像のARGBピクセル配列
    private int maskW;
    private int maskH;

    public UnderwearTextureCompositor() {}

    /**
     * マスクテクスチャをリソースから読み込んで初期化する。
     * Screen#init() から呼ぶ。
     */
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
                        // NativeImage は ABGR で返るので ARGB に変換
                        int abgr = img.getPixelRGBA(x, y);
                        int a = (abgr >> 24) & 0xFF;
                        int b = (abgr >> 16) & 0xFF;
                        int g = (abgr >>  8) & 0xFF;
                        int r =  abgr        & 0xFF;
                        maskPixels[y * maskW + x] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                }
                img.close();
            }

            dynamicTexture  = new DynamicTexture(maskW, maskH, true);
            String textureName = "tailormade_underwear_composite_" + COUNTER.getAndIncrement();
            textureLocation = Minecraft.getInstance()
                    .getTextureManager()
                    .register(textureName, dynamicTexture);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 選択色でテクスチャを再生成して ResourceLocation を返す。
     * 色変更のたびに呼ぶ。
     */
    public ResourceLocation compose(int argbColor) {
        if (dynamicTexture == null || maskPixels == null) return null;

        NativeImage img = dynamicTexture.getPixels();
        if (img == null) return null;

        int cr = (argbColor >> 16) & 0xFF;
        int cg = (argbColor >>  8) & 0xFF;
        int cb =  argbColor        & 0xFF;

        for (int y = 0; y < maskH; y++) {
            for (int x = 0; x < maskW; x++) {
                int src = maskPixels[y * maskW + x];
                int a   = (src >> 24) & 0xFF;

                if (a == 0) {
                    // 透明ピクセルはそのまま
                    img.setPixelRGBA(x, y, 0);
                } else {
                    // 不透明ピクセルは選択色に置き換え（ARGB→ABGR）
                    img.setPixelRGBA(x, y, (a << 24) | (cb << 16) | (cg << 8) | cr);
                }
            }
        }

        dynamicTexture.upload();
        return textureLocation;
    }

    public void close() {
        if (dynamicTexture != null) {
            dynamicTexture.close();
            dynamicTexture = null;
        }
    }
}
