package com.tailormade.tailor.data.records;

public record PixelEdit(int layerIndex, int[] beforePixels, int[] afterPixels) implements EditorActions {
}
