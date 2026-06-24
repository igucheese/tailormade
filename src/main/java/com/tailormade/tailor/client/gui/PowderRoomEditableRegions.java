package com.tailormade.tailor.client.gui;

import com.tailormade.tailor.utils.PixelCanvas;

import java.util.List;

public class PowderRoomEditableRegions {
    public record UVRect(int x, int y, int w, int h) {
        public boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    public static final List<UVRect> EDITABLE = List.of(
            new UVRect(16, 16, 24, 16), // 胴体
            new UVRect(40, 16, 16, 16), // 右腕
            new UVRect(32, 48, 16, 16), // 左腕
            new UVRect(0,  16, 16, 16), // 右足
            new UVRect(16, 48, 16, 16)  // 左足
    );

    public static boolean isEditable(int px, int py) {
        for (UVRect rect : EDITABLE) {
            if (rect.contains(px, py)) return true;
        }
        return false;
    }

    public static void lockNonEditablePixels(PixelCanvas canvas) {
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                if (!isEditable(x, y)) {
                    canvas.setPixel(x, y, PixelCanvas.TRANSPARENT, 1);
                }
            }
        }
    }
}
