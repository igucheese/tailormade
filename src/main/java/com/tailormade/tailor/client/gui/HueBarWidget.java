package com.tailormade.tailor.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class HueBarWidget {

    private static final int W = 16;
    private int H = 16;  // 後で高さだけ変えやすいよう定数化

    private final int x;
    private final int y;

    private int cursorY = 0;
    private int selectedHue = 0; // 0〜359

    private DynamicTexture texture;
    private ResourceLocation textureLocation;
    private boolean isDragging = false;

    public HueBarWidget(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void init() {
        if (texture != null) texture.close();
        texture         = new DynamicTexture(W, H, true);
        textureLocation = Minecraft.getInstance()
                .getTextureManager()
                .register("tailormade_huebar_widget", texture);
        rebuildTexture();
    }

    public void setH(int H) {
        this.H = H;
    }

    private void rebuildTexture() {
        NativeImage img = texture.getPixels();
        if (img == null) return;

        for (int py = 0; py < H; py++) {
            float hue = (float) py / (H - 1) * 360f;
            int rgb = hsvToRgb(hue, 1f, 1f);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >>  8) & 0xFF;
            int b =  rgb        & 0xFF;

            for (int px = 0; px < W; px++) {
                img.setPixelRGBA(px, py, (0xFF << 24) | (b << 16) | (g << 8) | r);
            }
        }
        texture.upload();
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        if (textureLocation == null) return;

        RenderSystem.enableBlend();
        g.blit(textureLocation, x, y, 0, 0, W, H, W, H);
        RenderSystem.disableBlend();

        // カーソル（白い横線）
        int cy = y + cursorY;
        g.fill(x, cy, x + W, cy + 1, 0xFFFFFFFF);
    }

    public boolean mousePressed(double mx, double my) {
        if (!inBounds(mx, my)) return false;
        isDragging = true;
        pick((int)(my - y));
        return true;
    }

    public boolean mouseDragged(double mx, double my) {
        if (!isDragging) return false;
        pick(Math.clamp((int)(my - y), 0, H - 1));
        return true;
    }

    public void mouseReleased() {
        isDragging = false;
    }

    private void pick(int py) {
        cursorY      = Math.clamp(py, 0, H - 1);
        selectedHue  = (int)((float) cursorY / (H - 1) * 359f);
    }

    private boolean inBounds(double mx, double my) {
        return mx >= x && mx < x + W && my >= y && my < y + H;
    }

    /** 選択中の色相から純色（彩度1・明度1）のARGBを返す */
    public int getSelectedBaseColor() {
        return 0xFF000000 | hsvToRgb(selectedHue, 1f, 1f);
    }

    public int getSelectedHue() { return selectedHue; }

    public void close() {
        if (texture != null) {
            texture.close();
            texture = null;
        }
    }

    // ---- HSV → RGB -------------------------------------------

    private static int hsvToRgb(float h, float s, float v) {
        float c  = v * s;
        float x  = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m  = v - c;

        float r, g, b;
        if      (h < 60)  { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else              { r = c; g = 0; b = x; }

        return ((int)((r + m) * 255) << 16)
                | ((int)((g + m) * 255) << 8)
                |  (int)((b + m) * 255);
    }
}
