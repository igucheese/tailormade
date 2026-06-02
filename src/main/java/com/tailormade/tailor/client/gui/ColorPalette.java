package com.tailormade.tailor.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.DyeColor;

import static com.tailormade.tailor.data.Constants.TRANSPARENT;

public class ColorPalette {
    public static final int ERASER = TRANSPARENT;
    private static final int SWATCH_SIZE = 8;
    private static final int SWATCH_MARGIN = 0;
    // バニラの染料の色をカラーパレットに反映するよ
    private static final int[] DYE_COLORS = buildDyeColors();

    private static int[] buildDyeColors() {
        DyeColor[] values = DyeColor.values();
        int[] colors = new int[values.length];
        for (DyeColor dye : values) {
            int c = dye.getTextureDiffuseColor();
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            colors[dye.ordinal()] = 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return colors;
    }

    private final int x;
    private final int y;

    private int selectedColor = 0xFF000000; // デフォルト黒
    private boolean eraserMode = false;
    private boolean isEraserEnabled = true;

    private int rValue = 0;
    private int gValue = 0;
    private int bValue = 0;

    public ColorPalette(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        for (int i = 0; i < DYE_COLORS.length; i++) {
            boolean isFirstColumn = i < DYE_COLORS.length / 2;
            int sx = isFirstColumn ? x : x + SWATCH_SIZE;
            int syMultiplier = isFirstColumn ? i : i - DYE_COLORS.length / 2;
            int sy = y + syMultiplier * (SWATCH_SIZE + SWATCH_MARGIN);
            drawSwatch(g, sx, sy, DYE_COLORS[i], !eraserMode && selectedColor == DYE_COLORS[i]);
        }
        if (this.isEraserEnabled) {
            int ey = eraserY();
            drawEraser(g, x, ey, eraserMode);
        }
    }

    private void drawSwatch(GuiGraphics g, int sx, int sy, int color, boolean selected) {
        g.fill(sx, sy, sx + SWATCH_SIZE, sy + SWATCH_SIZE, 0xFF000000);
        g.fill(sx + 1, sy + 1, sx + SWATCH_SIZE - 1, sy + SWATCH_SIZE - 1, color);
        if (selected) drawBorder(g, sx, sy, SWATCH_SIZE, SWATCH_SIZE, 0xFFFFFFFF);
    }

    private void drawEraser(GuiGraphics g, int sx, int sy, boolean selected) {
        int half = SWATCH_SIZE / 2;
        g.fill(sx, sy, sx + half, sy + half, 0xFFFFFFFF);
        g.fill(sx + half, sy, sx + SWATCH_SIZE, sy + half, 0xFF888888);
        g.fill(sx, sy + half, sx + half, sy + SWATCH_SIZE, 0xFF888888);
        g.fill(sx + half, sy + half, sx + SWATCH_SIZE, sy + SWATCH_SIZE, 0xFFFFFFFF);
        g.fill(sx, sy, sx + SWATCH_SIZE, sy + 1, 0xFF000000);
        g.fill(sx, sy + SWATCH_SIZE - 1, sx + SWATCH_SIZE, sy + SWATCH_SIZE, 0xFF000000);
        if (selected) drawBorder(g, sx, sy, SWATCH_SIZE, SWATCH_SIZE, 0xFFFFFFFF);
    }

    private void drawBorder(GuiGraphics g, int bx, int by, int w, int h, int color) {
        g.fill(bx, by, bx + w, by + 1, color);
        g.fill(bx, by + h - 1, bx + w, by + h, color);
        g.fill(bx, by, bx + 1, by + h, color);
        g.fill(bx + w - 1, by, bx + w, by + h, color);
    }

    private int eraserY() {
        return y + (DYE_COLORS.length / 2) * (SWATCH_SIZE + SWATCH_MARGIN);
    }

    public void disableEraser() {
        this.isEraserEnabled = false;
    }
    public void enableEraser() {
        this.isEraserEnabled = true;
    }

    public boolean mouseClicked(double mx, double my) {
        for (int i = 0; i < DYE_COLORS.length; i++) {
            boolean isFirstColumn = i < DYE_COLORS.length / 2;
            int sx = isFirstColumn ? x : x + SWATCH_SIZE;
            int syMultiplier = isFirstColumn ? i : i - DYE_COLORS.length / 2;
            int sy = y + syMultiplier * (SWATCH_SIZE + SWATCH_MARGIN);
            if (inBox(mx, my, sx, sy, SWATCH_SIZE, SWATCH_SIZE)) {
                selectedColor = DYE_COLORS[i];
                eraserMode = false;
                syncRgbFromColor();
                return true;
            }
        }
        int ey = eraserY();
        if (inBox(mx, my, x, ey, SWATCH_SIZE, SWATCH_SIZE)) {
            eraserMode = true;
            return true;
        }
        return false;
    }

    private boolean inBox(double mx, double my, int bx, int by, int w, int h) {
        return mx >= bx && mx < bx + w && my >= by && my < by + h;
    }

    public void setRgb(int r, int g, int b) {
        this.rValue = clamp(r);
        this.gValue = clamp(g);
        this.bValue = clamp(b);
        selectedColor = 0xFF000000 | (rValue << 16) | (gValue << 8) | bValue;
        eraserMode = false;
    }

    public void syncRgbFromColor() {
        rValue = (selectedColor >> 16) & 0xFF;
        gValue = (selectedColor >> 8) & 0xFF;
        bValue = selectedColor & 0xFF;
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    public void setEraserMode(boolean isEraserMode) { this.eraserMode = isEraserMode; }
    public void setSelectedColor(int color) { this.selectedColor = color; }

    public int getSelectedColor() { return selectedColor; }
    public boolean isEraserMode() { return eraserMode; }
    public int getRValue() { return rValue; }
    public int getGValue() { return gValue; }
    public int getBValue() { return bValue; }
}