package com.tailormade.tailor.data;

public enum PatternType {
    // ---- 頭（単一領域）---------------------------------------
    HEAD(new CanvasSegment[]{
            new CanvasSegment(0, 0, 0, 32, 16)
    }, "head"),

    // ---- 胴体 + 右腕 + 左腕 ----------------------------------
    // canvasX:  0 = 胴体(24), 24 = 右腕(16), 40 = 左腕(16)  → 計56x16
    CHEST(new CanvasSegment[]{
            new CanvasSegment(0,  16, 16, 24, 16),  // 胴体
            new CanvasSegment(24, 40, 16, 16, 16),  // 右腕
            new CanvasSegment(40, 32, 48, 16, 16)   // 左腕
    }, "chest"),

    // ---- 右脚 + 左脚 -----------------------------------------
    // canvasX:  0 = 右脚(16), 16 = 左脚(16)  → 計32x16
    LEGS(new CanvasSegment[]{
            new CanvasSegment(0,  0,  16, 16, 16),  // 右脚
            new CanvasSegment(16, 16, 48, 16, 16)   // 左脚
    }, "legs"),

    // ---- 右靴（右脚オーバーレイ）+ 左靴（左脚オーバーレイ）--
    // canvasX:  0 = 右靴(16), 16 = 左靴(16)  → 計32x16
    FEET(new CanvasSegment[]{
            new CanvasSegment(0,  0,  32, 16, 16),  // 右靴
            new CanvasSegment(16, 0,  48, 16, 16)   // 左靴
    }, "feet");

    // ---- CanvasSegment レコード --------------------------------

    public record CanvasSegment(int canvasX, int uvX, int uvY, int w, int h) {}

    // ---- フィールド -------------------------------------------

    private final CanvasSegment[] segments;
    private final int canvasW;
    private final int canvasH;
    private final String type;

    PatternType(CanvasSegment[] segments, String type) {
        this.segments = segments;
        int totalW = 0, maxH = 0;
        for (CanvasSegment s : segments) {
            totalW = Math.max(totalW, s.canvasX() + s.w());
            maxH   = Math.max(maxH, s.h());
        }
        this.canvasW = totalW;
        this.canvasH = maxH;
        this.type = type;
    }

    // ---- Getters ----------------------------------------------

    public CanvasSegment[] getSegments() { return segments; }
    public int getCanvasW() { return canvasW; }
    public int getCanvasH() { return canvasH; }
    public String getType() { return type; }

    /** 後方互換 */
    public int getUvX() { return segments[0].uvX(); }
    public int getUvY() { return segments[0].uvY(); }
    public int getTexW() { return canvasW; }
    public int getTexH() { return canvasH; }
    public int[] getTextureSize(){ return new int[]{canvasW, canvasH}; }
}