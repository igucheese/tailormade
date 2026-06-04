package com.tailormade.tailor.data;

public enum PatternType {

    HEAD(new CanvasSegment[]{
            new CanvasSegment(0, 0, 32, 0, 32, 16)
    }, "head"),

    CHEST(new CanvasSegment[]{
            // 上段：スキン
            new CanvasSegment(0,  0,  16, 16, 24, 16), // 胴体スキン
            new CanvasSegment(24, 0,  40, 16, 16, 16), // 右腕スキン
            new CanvasSegment(40, 0,  32, 48, 16, 16), // 左腕スキン
            // 下段：オーバーレイ
            new CanvasSegment(0,  16, 16, 32, 24, 16), // 胴体オーバーレイ
            new CanvasSegment(24, 16, 40, 32, 16, 16), // 右腕オーバーレイ
            new CanvasSegment(40, 16, 48, 48, 16, 16)  // 左腕オーバーレイ
    }, "chest"),

    LEGS(new CanvasSegment[]{
            // 上段：スキン
            new CanvasSegment(0,  0,  0,  16, 16, 16), // 右足スキン
            new CanvasSegment(16, 0,  16, 48, 16, 16), // 左足スキン
            // 下段：オーバーレイ
            new CanvasSegment(0,  16, 0,  32, 16, 16), // 右足オーバーレイ
            new CanvasSegment(16, 16, 0,  48, 16, 16)  // 左足オーバーレイ
    }, "legs"),

    FEET(new CanvasSegment[]{
            new CanvasSegment(0,  0, 8, 32, 4,  4), // 右靴底
            new CanvasSegment(0,  4, 0, 42, 16, 6), // 右靴側面下6
            new CanvasSegment(16, 0, 8, 48, 4,  4), // 左靴底
            new CanvasSegment(16, 4, 0, 58, 16, 6)  // 左靴側面下6
    }, "feet");

    public record CanvasSegment(
            int canvasX, int canvasY,  // キャンバス上の配置位置
            int uvX,     int uvY,      // スキンテクスチャ上の対応座標
            int w,       int h         // 領域サイズ
    ) {}

    private final CanvasSegment[] segments;
    private final int canvasW;
    private final int canvasH;
    private final String type;

    PatternType(CanvasSegment[] segments, String type) {
        this.segments = segments;
        int maxW = 0, maxH = 0;
        for (CanvasSegment s : segments) {
            maxW = Math.max(maxW, s.canvasX() + s.w());
            maxH = Math.max(maxH, s.canvasY() + s.h());
        }
        this.canvasW = maxW;
        this.canvasH = maxH;
        this.type = type;
    }

    public CanvasSegment[] getSegments() { return segments; }
    public int getCanvasW() { return canvasW; }
    public int getCanvasH() { return canvasH; }
    public String getType() { return type; }

    /** 後方互換 */
    public int getTexW()          { return canvasW; }
    public int getTexH()          { return canvasH; }
    public int getUvX()           { return segments[0].uvX(); }
    public int getUvY()           { return segments[0].uvY(); }
    public int[] getTextureSize() { return new int[]{canvasW, canvasH}; }
}