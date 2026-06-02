package com.tailormade.tailor.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static com.tailormade.tailor.data.Constants.DEFAULT_COLOR;
import static com.tailormade.tailor.data.Constants.TRANSPARENT;

/**
 * ドットエディタのピクセルデータ管理と DynamicTexture 更新を担う。
 *
 * 内部は ARGB (0xAARRGGBB) で保持。
 * NativeImage は ABGR (0xAABBGGRR) 形式なので、upload時に変換する。
 */
public class PixelCanvas {

    public static final int TRANSPARENT  = 0x00000000;
    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    private static final int HISTORY_MAX = 20;

    private final int width;
    private final int height;
    private final int[] pixels; // 現在の状態 (ARGB)

    // Undo スタック: snapshot() を呼ぶたびに現在状態をプッシュ
    private final Deque<int[]> undoStack = new ArrayDeque<>();
    // Redo スタック: undo() 時に現在状態をプッシュ、新規描画時にクリア
    private final Deque<int[]> redoStack = new ArrayDeque<>();

    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;
    private boolean dirty = false;

    public PixelCanvas(int width, int height) {
        this.width  = width;
        this.height = height;
        this.pixels = new int[width * height];
        Arrays.fill(this.pixels, DEFAULT_COLOR);
    }

    public void init() {
        if (dynamicTexture != null) dynamicTexture.close();
        dynamicTexture  = new DynamicTexture(width, height, true);
        textureLocation = Minecraft.getInstance()
                .getTextureManager()
                .register("tailor_canvas", dynamicTexture);
        uploadAll();
        undoStack.push(pixels.clone());
    }

    // ---- ピクセル操作 ----------------------------------------

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
        try {
            pixels[y * width + x] = argbColor;
        } catch (Exception e) {}
    }

    public void fill(int argbColor) {
        for (int i = 0; i < pixels.length; i++) {
            try {
                pixels[i] = argbColor;
            } catch (Exception e) {}
        }
        dirty = true;
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

    // ---- Undo / Redo -----------------------------------------

    /**
     * 現在の状態を Undo スタックに積む。
     * DesignerScreen の mouseReleased() から呼ぶ（ストローク確定時）。
     * Redo スタックはクリアする。
     */
    public void snapshot() {
        if (undoStack.size() >= HISTORY_MAX) undoStack.pollLast();
        undoStack.push(pixels.clone());
        redoStack.clear();
    }

    /**
     * Undo: 1つ前の状態に戻す。
     * 現在状態を Redo スタックに退避してから復元する。
     */
    public void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.push(pixels.clone());
        restore(undoStack.pop());
    }

    /**
     * Redo: undo した操作をやり直す。
     */
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

    // ---- DynamicTexture 更新 ---------------------------------

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
                int b =  argb        & 0xFF;
                img.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        dynamicTexture.upload();
    }

    // ---- データ入出力 ----------------------------------------

    public int[] getPixels()          { return pixels.clone(); }
    public void loadPixels(int[] data) {
        if (data == null || data.length != pixels.length) return;
        System.arraycopy(data, 0, pixels, 0, pixels.length);
        undoStack.clear();
        redoStack.clear();
        dirty = true;
    }

    // ---- Getters / Lifecycle ---------------------------------

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
