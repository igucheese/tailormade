package com.tailormade.tailor.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class ColorPickerWidget {

    private static final int SIZE = 16;

    private final int x;
    private final int y;

    private int baseColor = 0xFFFFFF00;
    private int selectedColor = 0xFFFFFFFF;

    private DynamicTexture texture;
    private ResourceLocation textureLocation;

    private int cursorX = SIZE - 1;
    private int cursorY = 0;

    public ColorPickerWidget(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void init() {
        if (texture != null) texture.close();
        texture         = new DynamicTexture(SIZE, SIZE, true);
        textureLocation = Minecraft.getInstance()
                .getTextureManager()
                .register("tailormade_colorpicker", texture);
        rebuildTexture();
    }

    public void setBaseColor(int argb) {
        this.baseColor = argb;
        rebuildTexture();
        selectedColor = sampleColor(cursorX, cursorY);
    }

    private void rebuildTexture() {
        NativeImage img = texture.getPixels();
        if (img == null) return;

        int br = (baseColor >> 16) & 0xFF;
        int bg = (baseColor >>  8) & 0xFF;
        int bb =  baseColor        & 0xFF;

        for (int py = 0; py < SIZE; py++) {
            float brightness = 1.0f - (float) py / (SIZE - 1);

            for (int px = 0; px < SIZE; px++) {
                float saturation = (float) px / (SIZE - 1);

                int r = (int)((1.0f - saturation) * 255 + saturation * br);
                int g = (int)((1.0f - saturation) * 255 + saturation * bg);
                int b = (int)((1.0f - saturation) * 255 + saturation * bb);

                r = (int)(r * brightness);
                g = (int)(g * brightness);
                b = (int)(b * brightness);

                img.setPixelRGBA(px, py, (0xFF << 24) | (b << 16) | (g << 8) | r);
            }
        }
        texture.upload();
    }

    private int sampleColor(int px, int py) {
        int br = (baseColor >> 16) & 0xFF;
        int bg = (baseColor >>  8) & 0xFF;
        int bb =  baseColor        & 0xFF;

        float brightness = 1.0f - (float) py / (SIZE - 1);
        float saturation = (float) px / (SIZE - 1);

        int r = (int)((1.0f - saturation) * 255 + saturation * br);
        int g = (int)((1.0f - saturation) * 255 + saturation * bg);
        int b = (int)((1.0f - saturation) * 255 + saturation * bb);

        r = Math.clamp((int)(r * brightness), 0, 255);
        g = Math.clamp((int)(g * brightness), 0, 255);
        b = Math.clamp((int)(b * brightness), 0, 255);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (textureLocation == null) return;

        RenderSystem.enableBlend();
        g.blit(textureLocation, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
        RenderSystem.disableBlend();

        // カーソル点を描画しておく！
        int cx = x + cursorX;
        int cy = y + cursorY;
        g.fill(cx,     cy,     cx + 1, cy + 1, 0xFFFFFFFF);
    }

    public boolean mouseClicked(double mx, double my) {
        if (!inBounds(mx, my)) return false;
        pick((int)(mx - x), (int)(my - y));
        return true;
    }

    public boolean mouseDragged(double mx, double my) {
        if (!isDragging) return false;
        int px = Math.clamp((int)(mx - x), 0, SIZE - 1);
        int py = Math.clamp((int)(my - y), 0, SIZE - 1);
        pick(px, py);
        return true;
    }

    private boolean isDragging = false;

    public boolean mousePressed(double mx, double my) {
        if (!inBounds(mx, my)) return false;
        isDragging = true;
        pick((int)(mx - x), (int)(my - y));
        return true;
    }

    public void mouseReleased() {
        isDragging = false;
    }

    private void pick(int px, int py) {
        cursorX      = Math.clamp(px, 0, SIZE - 1);
        cursorY      = Math.clamp(py, 0, SIZE - 1);
        selectedColor = sampleColor(cursorX, cursorY);
    }

    private boolean inBounds(double mx, double my) {
        return mx >= x && mx < x + SIZE && my >= y && my < y + SIZE;
    }

    public int getSelectedColor() { return selectedColor; }

    public void close() {
        if (texture != null) {
            texture.close();
            texture = null;
        }
    }
}
