package com.tailormade.tailor.utils;

import com.tailormade.tailor.data.records.LayerData;
import com.tailormade.tailor.data.PixelData;

import java.util.List;

public class LayerService {
    public static LayerData newLayer(int[] pixels) {
        return newLayer(1, 1, new PixelData(pixels));
    }

    public static LayerData newLayer(int id, int layerIndex, PixelData pixelData) {
        return newLayer(id, layerIndex, pixelData, true);
    }

    public static LayerData newLayer(int id, int layerIndex, PixelData pixelData, boolean isVisible) {
        return newLayer(id, layerIndex, pixelData, isVisible, 100);
    }

    public static LayerData newLayer(int id, int layerIndex, PixelData pixelData, boolean isVisible, int alpha) {
        return new LayerData(
                id, layerIndex, isVisible, pixelData, alpha
        );
    }
}
