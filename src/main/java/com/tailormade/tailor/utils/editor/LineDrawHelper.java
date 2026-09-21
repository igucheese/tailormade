package com.tailormade.tailor.utils.editor;

import java.util.ArrayList;
import java.util.List;

public class LineDrawHelper {
    public static List<int[]> getLinePixels(int x0, int y0, int x1, int y1) {
        List<int[]> result = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;

        int x = x0, y = y0;
        while (true) {
            result.add(new int[]{x, y});
            if (x == x1 && y == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y += sy;
            }
        }
        return result;
    }
}
