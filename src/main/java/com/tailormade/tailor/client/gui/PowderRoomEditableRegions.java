package com.tailormade.tailor.client.gui;

import com.tailormade.tailor.utils.PixelCanvas;

import java.util.List;

public class PowderRoomEditableRegions {
    public record UVRect(int x, int y, int w, int h) {
        public boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    /** 編集可能な UV 矩形の一覧 */
    public static final List<UVRect> EDITABLE = List.of(
            new UVRect(16, 16, 24, 16), // 胴体スキン
            new UVRect(40, 16, 16, 16), // 右腕スキン
            new UVRect(32, 48, 16, 16), // 左腕スキン
            new UVRect(0,  16, 16, 16), // 右足スキン
            new UVRect(16, 48, 16, 16)  // 左足スキン
    );

    /** 指定ピクセルが編集可能か */
    public static boolean isEditable(int px, int py) {
        for (UVRect rect : EDITABLE) {
            if (rect.contains(px, py)) return true;
        }
        return false;
    }

    /**
     * キャンバス全体（64x64）のうち編集不可ピクセルをすべて透明に初期化する。
     * PowderRoomScreen の initCanvas() 内で呼ぶ。
     */
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
