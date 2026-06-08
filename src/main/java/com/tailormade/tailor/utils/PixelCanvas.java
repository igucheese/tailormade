package com.tailormade.tailor.utils;

import com.mojang.blaze3d.platform.NativeImage;
import com.tailormade.tailor.client.gui.PowderRoomEditableRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;

import static com.tailormade.tailor.data.Constants.DEFAULT_COLOR;
import static com.tailormade.tailor.data.Constants.TRANSPARENT;

public class PixelCanvas {

    public static final int TRANSPARENT = 0x00000000;
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    private static final int HISTORY_MAX = 20;

    private final int width;
    private final int height;
    private final int[] pixels;

    private final Deque<int[]> undoStack = new ArrayDeque<>();
    private final Deque<int[]> redoStack = new ArrayDeque<>();

    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;
    private boolean dirty = false;
    private boolean isSkin = false;

    public PixelCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        Arrays.fill(this.pixels, DEFAULT_COLOR);
    }

    public void init() {
        if (dynamicTexture != null) dynamicTexture.close();
        dynamicTexture = new DynamicTexture(width, height, true);
        textureLocation = Minecraft.getInstance()
                .getTextureManager()
                .register("tailor_canvas", dynamicTexture);
        uploadAll();
        undoStack.push(pixels.clone());
    }

    public void setPixel(int x, int y, int argbColor, int brushSize) {
        if (!inBounds(x, y)) return;
        pixels[y * width + x] = argbColor;
        if (brushSize > 1) {
            if (brushSize == 2) {
                trySetPixel(x + 1, y, argbColor);
                trySetPixel(x, y + 1, argbColor);
                trySetPixel(x + 1, y + 1, argbColor);
            } else {
                trySetPixel(x + 1, y, argbColor);
                trySetPixel(x - 1, y, argbColor);
                trySetPixel(x - 1, y - 1, argbColor);
                trySetPixel(x, y - 1, argbColor);
                trySetPixel(x + 1, y - 1, argbColor);
                trySetPixel(x - 1, y + 1, argbColor);
                trySetPixel(x, y + 1, argbColor);
                trySetPixel(x + 1, y + 1, argbColor);
            }
        }
        dirty = true;
    }

    private void trySetPixel(int x, int y, int argbColor) {
        if (isSkin && !PowderRoomEditableRegions.isEditable(x, y)) return;
        try {
            pixels[y * width + x] = argbColor;
        } catch (Exception e) {}
    }

    public void fill(int argbColor) {
        for (int i = 0; i < pixels.length; i++) {
            int[] px = convertIndexToXY(i);
            trySetPixel(px[0], px[1], argbColor);
        }
        dirty = true;
    }

    private int[] convertIndexToXY(int i) {
        int x = i % width;
        int y = (i - x) / width;
        int[] coords = {x, y};
        return coords;
    }

    public int getPixel(int x, int y) {
        if (!inBounds(x, y)) return TRANSPARENT;
        return pixels[y * width + x];
    }

    public void erase(int x, int y, int brushSize) {
        setPixel(x, y, TRANSPARENT, brushSize);
    }
    private boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    public void setIsSkin(boolean isSkin) {
        this.isSkin = isSkin;
    }

    public void snapshot() {
        if (undoStack.size() >= HISTORY_MAX) undoStack.pollLast();
        undoStack.push(pixels.clone());
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.push(pixels.clone());
        restore(undoStack.pop());
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.push(pixels.clone());
        restore(redoStack.pop());
    }

    private void restore(int[] saved) {
        System.arraycopy(saved, 0, pixels, 0, pixels.length);
        dirty = true;
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public void uploadIfDirty() {
        if (dirty) { uploadAll(); dirty = false; }
    }

    private void uploadAll() {
        NativeImage img = dynamicTexture.getPixels();
        if (img == null) return;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixels[y * width + x];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >>  8) & 0xFF;
                int b =  argb & 0xFF;
                img.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        dynamicTexture.upload();
    }

    public int[] getPixels() { return pixels.clone(); }
    public void loadPixels(int[] data) {
        if (data == null || data.length != pixels.length) return;
        System.arraycopy(data, 0, pixels, 0, pixels.length);
        undoStack.clear();
        redoStack.clear();
        dirty = true;
    }

    public ResourceLocation getTextureLocation() { return textureLocation; }
    public int getWidth()  { return width; }
    public int getHeight() { return height; }

    public void close() {
        if (dynamicTexture != null) {
            dynamicTexture.close();
            dynamicTexture = null;
        }
    }
}
