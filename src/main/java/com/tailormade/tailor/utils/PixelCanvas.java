package com.tailormade.tailor.utils;

import com.mojang.blaze3d.platform.NativeImage;
import com.tailormade.tailor.client.gui.PowderRoomEditableRegions;
import com.tailormade.tailor.data.PixelData;
import com.tailormade.tailor.data.records.EditorActions;
import com.tailormade.tailor.data.records.LayerData;
import com.tailormade.tailor.data.records.LayerStructureChange;
import com.tailormade.tailor.data.records.PixelEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.tailormade.tailor.data.Constants.DEFAULT_COLOR;
import static com.tailormade.tailor.data.Constants.TRANSPARENT;

public class PixelCanvas {
    public record Layer(boolean isVisible, int[] pixelData, int alpha) {}

    public static final int TRANSPARENT = 0x00000000;
    public static final int DEFAULT_COLOR = 0x00000000;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private static final int HISTORY_MAX = 20;

    private final int width;
    private final int height;

    private final List<Layer> layers = new ArrayList<>();
    private final int[] compositedPixels;
    private int activeLayerIndex = 0;

    private final Deque<EditorActions> undoStack = new ArrayDeque<>();
    private final Deque<EditorActions> redoStack = new ArrayDeque<>();

    private DynamicTexture dynamicTexture;
    private ResourceLocation textureLocation;
    private boolean dirty = false;
    private boolean isSkin = false;

    // レイヤー縮小表示用
    private final Map<Integer, DynamicTexture> layerThumbnails = new HashMap<>();

    public PixelCanvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.addLayer();
        this.compositedPixels = new int[width * height];
        recomposite();
    }

    public void init() {
        if (dynamicTexture != null) dynamicTexture.close();
        dynamicTexture = new DynamicTexture(width, height, true);
        String textureName = "tailor_canvas_" + COUNTER.getAndIncrement();
        textureLocation = Minecraft.getInstance()
                .getTextureManager()
                .register(textureName, dynamicTexture);
        uploadAll();
        int[] snapshot = layers.get(activeLayerIndex).pixelData().clone();
        undoStack.push(new PixelEdit(activeLayerIndex, snapshot, snapshot));
    }

    public void setPixel(int x, int y, int argbColor, int brushSize) {
        int[] active = layers.get(activeLayerIndex).pixelData();
        if (!inBounds(x, y)) return;
        active[y * width + x] = argbColor;
        if (brushSize > 1) {
            if (brushSize == 2) {
                trySetPixel(active, x + 1, y, argbColor);
                trySetPixel(active, x, y + 1, argbColor);
                trySetPixel(active, x + 1, y + 1, argbColor);
            } else {
                trySetPixel(active, x + 1, y, argbColor);
                trySetPixel(active, x - 1, y, argbColor);
                trySetPixel(active, x - 1, y - 1, argbColor);
                trySetPixel(active, x, y - 1, argbColor);
                trySetPixel(active, x + 1, y - 1, argbColor);
                trySetPixel(active, x - 1, y + 1, argbColor);
                trySetPixel(active, x, y + 1, argbColor);
                trySetPixel(active, x + 1, y + 1, argbColor);
            }
        }
        markDirty();
    }

    private void trySetPixel(int[] target, int x, int y, int argbColor) {
        if (isSkin && !PowderRoomEditableRegions.isEditable(x, y)) return;
        try {
            target[convertXYToIndex(x, y)] = argbColor;
        } catch (Exception e) {}
    }

    public void fill(int argbColor) {
        int[] active = layers.get(activeLayerIndex).pixelData();
        for (int i = 0; i < active.length; i++) {
            int[] px = convertIndexToXY(i);
            trySetPixel(active, px[0], px[1], argbColor);
        }
        markDirty();
    }

    public void fill(int startX, int startY, int argbColor) {
        int[] active = layers.get(activeLayerIndex).pixelData();
        int startIndex = convertXYToIndex(startX, startY);
        if (startIndex < 0 || startIndex >= active.length) return;

        int targetColor = active[startIndex];
        if (targetColor == argbColor) return;

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] pos = stack.pop();
            int x = pos[0];
            int y = pos[1];
            if (x < 0 || x >= width || y < 0 || y >= height) continue;

            int index = convertXYToIndex(x, y);
            if (active[index] != targetColor) continue;

            trySetPixel(active, x, y, argbColor);

            stack.push(new int[]{x + 1, y});
            stack.push(new int[]{x - 1, y});
            stack.push(new int[]{x, y + 1});
            stack.push(new int[]{x, y - 1});
        }

        markDirty();
    }

    public int getPixel(int x, int y) {
        if (!inBounds(x, y)) return TRANSPARENT;
        return compositedPixels[y * width + x];
    }

    public void erase(int x, int y, int brushSize) {
        setPixel(x, y, TRANSPARENT, brushSize);
    }

    public void setActiveLayerIndex(int index) {
        this.activeLayerIndex = index;
    }
    public int[] getActiveLayerPixels() {
        return this.layers.get(this.activeLayerIndex).pixelData();
    }
    public int[] getLayerPixels(int index) {
        if (index >= layers.size() || index < 0) return null;
        return this.layers.get(index).pixelData();
    }
    public int getActiveLayerIndex() { return this.activeLayerIndex; }
    public List<LayerData> layerAsData() {
        List<LayerData> lds = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            lds.add(LayerService.newLayer(
                    i, i, new PixelData(layers.get(i).pixelData()), layers.get(i).isVisible()
            ));
        }
        return lds;
    }
    public void addLayer() {
        if (!canAddLayer()) return;
        List<LayerData> before = layerAsData();
        int[] base = new int[width * height];
        Arrays.fill(base, DEFAULT_COLOR);
        this.layers.add(new Layer(true, base, 100));
        this.activeLayerIndex = this.layers.size() - 1;
        List<LayerData> after = layerAsData();
        undoStack.push(new LayerStructureChange(before, after));
    }
    public void removeLayer(int index) {
        if (this.layers.size() <= 1) return;
        List<LayerData> before = layerAsData();
        if (index == activeLayerIndex) {
            if (activeLayerIndex == 0) {
                activeLayerIndex = layers.size() - 2;
            } else {
                activeLayerIndex--;
            }
        }
        this.layers.remove(index);
        markDirty();
        List<LayerData> after = layerAsData();
        undoStack.push(new LayerStructureChange(before, after));
    }
    public int moveLayer(boolean isUp) {
        int nextIndex = isUp ? activeLayerIndex + 1 : activeLayerIndex - 1;
        if (isUp && layers.size() <= nextIndex) return -1;
        if (!isUp && nextIndex < 0) return -1;

        Collections.swap(layers, activeLayerIndex, nextIndex);
        markDirty();
        return nextIndex;
    }
    public boolean canAddLayer() {
        return this.layers.size() < 8;
    }
    public void loadLayers(List<LayerData> savedLayers) {
        for (int i = 0; i < savedLayers.size(); i++) {
            LayerData ld = savedLayers.get(i);
            if (i == 0) {
                this.layers.set(i, new Layer(ld.isVisible(), ld.pixelData().getPixels(), ld.alpha()));
            } else {
                this.layers.add(new Layer(ld.isVisible(), ld.pixelData().getPixels(), ld.alpha()));
            }
        }
        for (LayerData sl: savedLayers) {
            restoreLayer(sl.layerIndex(), sl.pixelData().getPixels());
        }
        markDirty();
    }
    public void toggleVisibility(int index) {
        Layer layer = layers.get(index);
        layers.set(index, new Layer(!layer.isVisible(), layer.pixelData(), layer.alpha()));
        markDirty();
    }
    public boolean isLayerVisible(int index) {
        if (index >= layers.size() || index < 0) return true;
        return layers.get(index).isVisible();
    }

    private void recomposite() {
        Arrays.fill(compositedPixels, TRANSPARENT);
        for (Layer layer : layers) {
            if (!layer.isVisible) continue;
            for (int i = 0; i < compositedPixels.length; i++) {
                int top = layer.pixelData()[i];
                int topAlpha = (top >>> 24) & 0xFF;
                if (topAlpha == 0) continue;
                if (topAlpha == 255) {
                    compositedPixels[i] = top;
                } else {
                    compositedPixels[i] = alphaBlend(compositedPixels[i], top, topAlpha);
                }
            }
        }
    }

    private int alphaBlend(int dst, int src, int srcAlpha) {
        float a = srcAlpha / 255f;
        int dstA = (dst >>> 24) & 0xFF;
        int dstR = (dst >> 16) & 0xFF, dstG = (dst >> 8) & 0xFF, dstB = dst & 0xFF;
        int srcR = (src >> 16) & 0xFF, srcG = (src >> 8) & 0xFF, srcB = src & 0xFF;

        int outR = Math.round(srcR * a + dstR * (1 - a));
        int outG = Math.round(srcG * a + dstG * (1 - a));
        int outB = Math.round(srcB * a + dstB * (1 - a));
        int outA = Math.round(255 * a + dstA * (1 - a));

        return (outA << 24) | (outR << 16) | (outG << 8) | outB;
    }

    private void markDirty() {
        recomposite();
        dirty = true;
    }

    private int[] pendingBeforeSnapshot = null;
    public void snapshot() {
        pendingBeforeSnapshot = layers.get(activeLayerIndex).pixelData().clone();
    }

    public void commitPendingAction() {
        if (pendingBeforeSnapshot == null) return;
        int[] afterState = layers.get(activeLayerIndex).pixelData().clone();
        if (!Arrays.equals(pendingBeforeSnapshot, afterState)) {
            if (undoStack.size() >= HISTORY_MAX) undoStack.pollLast();
            undoStack.push(new PixelEdit(activeLayerIndex, pendingBeforeSnapshot, afterState));
            redoStack.clear();
        }

        pendingBeforeSnapshot = null;
    }

    public void undo() {
        if (undoStack.isEmpty()) return;
        EditorActions action = undoStack.pop();
        if (action instanceof PixelEdit edit) {
            int[] currentState = layers.get(edit.layerIndex()).pixelData().clone();
            redoStack.push(new PixelEdit(edit.layerIndex(), edit.beforePixels(), currentState));
            restoreLayer(edit.layerIndex(), edit.beforePixels());
        } else if (action instanceof LayerStructureChange change) {
            redoStack.push(new LayerStructureChange(change.afterLayers(), change.beforeLayers()));
            restoreLayers(change.beforeLayers());
        }
    }

    public void redo() {
        if (redoStack.isEmpty()) return;
        EditorActions action = redoStack.pop();
        if (action instanceof PixelEdit edit) {
            int[] currentState = layers.get(edit.layerIndex()).pixelData().clone();
            undoStack.push(new PixelEdit(edit.layerIndex(), currentState, edit.afterPixels()));
            restoreLayer(edit.layerIndex(), edit.afterPixels());
        } else if (action instanceof LayerStructureChange change) {
            undoStack.push(new LayerStructureChange(change.afterLayers(), change.beforeLayers()));
            restoreLayers(change.afterLayers());
        }
    }

    private void restoreLayers(List<LayerData> savedLayers) {
        this.layers.clear();
        for (LayerData l: savedLayers) {
            this.layers.add(new Layer(l.isVisible(), l.pixelData().getPixels(), l.alpha()));
        }
        markDirty();
    }
    private void restoreLayer(int layerIndex, int[] saved) {
        if (layers.size() <= layerIndex) return;
        System.arraycopy(saved, 0, layers.get(layerIndex).pixelData(), 0, saved.length);
        markDirty();
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
                int argb = compositedPixels[y * width + x];
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >>  8) & 0xFF;
                int b =  argb & 0xFF;
                img.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        dynamicTexture.upload();
    }

    private int[] convertIndexToXY(int i) {
        int x = i % width;
        int y = (i - x) / width;
        return new int[]{x, y};
    }

    private int convertXYToIndex(int x, int y) {
        return y * width + x;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public void setIsSkin(boolean isSkin) {
        this.isSkin = isSkin;
    }

    public int[] getPixels() { return compositedPixels.clone(); }

    public void loadPixels(int[] data) {
        if (data == null || data.length != compositedPixels.length) return;
        System.arraycopy(data, 0, layers.get(0).pixelData(), 0, data.length);
        undoStack.clear();
        redoStack.clear();
        markDirty();
    }

    public List<Layer> getLayers() { return layers; }
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
