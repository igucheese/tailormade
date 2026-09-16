package com.tailormade.tailor.data;

public enum PatternType {
    HEAD(
            new CanvasSegment[]{
                    new CanvasSegment(0, 0, 32, 0, 32, 16)
            },
            "head",
            new FaceSegment[]{
                    new FaceSegment( 8,  0,  8,  8, "Top"),
                    new FaceSegment(16,  0,  8,  8, "Bot"),
                    new FaceSegment( 0,  8,  8,  8, "Right"),
                    new FaceSegment( 8,  8,  8,  8, "Front"),
                    new FaceSegment(16,  8,  8,  8, "Left"),
                    new FaceSegment(24,  8,  8,  8, "Back")
            },
            new FaceSegment[]{
                    new FaceSegment( 8,  0,  8,  8, "Top"),
                    new FaceSegment(16,  0,  8,  8, "Bot"),
                    new FaceSegment( 0,  8,  8,  8, "Right"),
                    new FaceSegment( 8,  8,  8,  8, "Front"),
                    new FaceSegment(16,  8,  8,  8, "Left"),
                    new FaceSegment(24,  8,  8,  8, "Back")
            }
    ),

    CHEST(
            new CanvasSegment[]{
                    // 上段：スキン
                    new CanvasSegment(0,  0,  16, 16, 24, 16), // 胴体スキン
                    new CanvasSegment(24, 0,  40, 16, 16, 16), // 右腕スキン
                    new CanvasSegment(40, 0,  32, 48, 16, 16), // 左腕スキン
                    // 下段：オーバーレイ
                    new CanvasSegment(0,  16, 16, 32, 24, 16), // 胴体オーバーレイ
                    new CanvasSegment(24, 16, 40, 32, 16, 16), // 右腕オーバーレイ
                    new CanvasSegment(40, 16, 48, 48, 16, 16)  // 左腕オーバーレイ
            },
            "chest",
            new FaceSegment[]{
                    // 胴体スキン
                    new FaceSegment(4,  0,  8,  4, "Top"),
                    new FaceSegment(12,  0,  8,  4, "Bot"),
                    new FaceSegment(0,  4,  4, 12, "Right"),
                    new FaceSegment(4,  4,  8, 12, "Front"),
                    new FaceSegment(12,  4,  4, 12, "Left"),
                    new FaceSegment(16,  4,  8, 12, "Back"),
                    // 右腕スキン
                    new FaceSegment(28,  0,  4,  4, "Top"),
                    new FaceSegment(32,  0,  4,  4, "Bot"),
                    new FaceSegment(24,  4,  4, 12, "Right"),
                    new FaceSegment(28,  4,  4, 12, "Front"),
                    new FaceSegment(32,  4,  4, 12, "Left"),
                    new FaceSegment(36,  4,  4, 12, "Back"),
                    // 左腕スキン
                    new FaceSegment(44,  0,  4,  4, "Top"),
                    new FaceSegment(48,  0,  4,  4, "Bot"),
                    new FaceSegment(40,  4,  4, 12, "Right"),
                    new FaceSegment(44,  4,  4, 12, "Front"),
                    new FaceSegment(48,  4,  4, 12, "Left"),
                    new FaceSegment(52,  4,  4, 12, "Back"),

                    // 胴体スキン
                    new FaceSegment(4,  16,  8,  4, "Top"),
                    new FaceSegment(12,  16,  8,  4, "Bot"),
                    new FaceSegment(0,  20,  4, 12, "Right"),
                    new FaceSegment(4,  20,  8, 12, "Front"),
                    new FaceSegment(12,  20,  4, 12, "Left"),
                    new FaceSegment(16,  20,  8, 12, "Back"),
                    // 右腕スキン
                    new FaceSegment(28,  16,  4,  4, "Top"),
                    new FaceSegment(32,  16,  4,  4, "Bot"),
                    new FaceSegment(24,  20,  4, 12, "Right"),
                    new FaceSegment(28,  20,  4, 12, "Front"),
                    new FaceSegment(32,  20,  4, 12, "Left"),
                    new FaceSegment(36,  20,  4, 12, "Back"),
                    // 左腕スキン
                    new FaceSegment(44,  16,  4,  4, "Top"),
                    new FaceSegment(48,  16,  4,  4, "Bot"),
                    new FaceSegment(40,  20,  4, 12, "Right"),
                    new FaceSegment(44,  20,  4, 12, "Front"),
                    new FaceSegment(48,  20,  4, 12, "Left"),
                    new FaceSegment(52,  20,  4, 12, "Back")
            },
            new FaceSegment[]{
                    // 胴体スキン
                    new FaceSegment(4,  0,  8,  4, "Top"),
                    new FaceSegment(12,  0,  8,  4, "Bot"),
                    new FaceSegment(0,  4,  4, 12, "Right"),
                    new FaceSegment(4,  4,  8, 12, "Front"),
                    new FaceSegment(12,  4,  4, 12, "Left"),
                    new FaceSegment(16,  4,  8, 12, "Back"),
                    // 右腕スキン
                    new FaceSegment(28,  0,  3,  4, "Top"),
                    new FaceSegment(31,  0,  3,  4, "Bot"),
                    new FaceSegment(24,  4,  4, 12, "Right"),
                    new FaceSegment(28,  4,  3, 12, "Front"),
                    new FaceSegment(31,  4,  4, 12, "Left"),
                    new FaceSegment(35,  4,  3, 12, "Back"),
                    // 左腕スキン
                    new FaceSegment(44,  0,  3,  4, "Top"),
                    new FaceSegment(47,  0,  3,  4, "Bot"),
                    new FaceSegment(40,  4,  4, 12, "Right"),
                    new FaceSegment(44,  4,  3, 12, "Front"),
                    new FaceSegment(47,  4,  4, 12, "Left"),
                    new FaceSegment(51,  4,  3, 12, "Back"),

                    // 胴体スキン
                    new FaceSegment(4,  16,  8,  4, "Top"),
                    new FaceSegment(12,  16,  8,  4, "Bot"),
                    new FaceSegment(0,  20,  4, 12, "Right"),
                    new FaceSegment(4,  20,  8, 12, "Front"),
                    new FaceSegment(12,  20,  4, 12, "Left"),
                    new FaceSegment(16,  20,  8, 12, "Back"),
                    // 右腕スキン
                    new FaceSegment(28,  16,  3,  4, "Top"),
                    new FaceSegment(31,  16,  3,  4, "Bot"),
                    new FaceSegment(24,  20,  4, 12, "Right"),
                    new FaceSegment(28,  20,  3, 12, "Front"),
                    new FaceSegment(31,  20,  4, 12, "Left"),
                    new FaceSegment(35,  20,  3, 12, "Back"),
                    // 左腕スキン
                    new FaceSegment(44,  16,  3,  4, "Top"),
                    new FaceSegment(47,  16,  3,  4, "Bot"),
                    new FaceSegment(40,  20,  4, 12, "Right"),
                    new FaceSegment(44,  20,  3, 12, "Front"),
                    new FaceSegment(47,  20,  4, 12, "Left"),
                    new FaceSegment(51,  20,  3, 12, "Back")
            }
    ),

    LEGS(
            new CanvasSegment[]{
                    // 上段：スキン
                    new CanvasSegment(0,  0,  0,  16, 16, 16), // 右足スキン
                    new CanvasSegment(16, 0,  16, 48, 16, 16), // 左足スキン
                    // 下段：オーバーレイ
                    new CanvasSegment(0,  16, 0,  32, 16, 16), // 右足オーバーレイ
                    new CanvasSegment(16, 16, 0,  48, 16, 16)  // 左足オーバーレイ
            },
            "legs",
            new FaceSegment[]{
                    // 右足スキン
                    new FaceSegment( 4,  0,  4,  4, "Top"),
                    new FaceSegment( 8,  0,  4,  4, "Bot"),
                    new FaceSegment( 0,  4,  4, 12, "Right"),
                    new FaceSegment( 4,  4,  4, 12, "Front"),
                    new FaceSegment( 8,  4,  4, 12, "Left"),
                    new FaceSegment( 12,  4,  4, 12, "Back"),

                    // 左足スキン
                    new FaceSegment(20,  0,  4,  4, "Top"),
                    new FaceSegment(24,  0,  4,  4, "Bot"),
                    new FaceSegment(16,  4,  4, 12, "Right"),
                    new FaceSegment(20,  4,  4, 12, "Front"),
                    new FaceSegment(24,  4,  4, 12, "Left"),
                    new FaceSegment(28,  4,  4, 12, "Back"),

                    // 右足オーバーレイ
                    new FaceSegment( 4,  16,  4,  4, "Top"),
                    new FaceSegment( 8,  16,  4,  4, "Bot"),
                    new FaceSegment( 0,  20,  4, 12, "Right"),
                    new FaceSegment( 4,  20,  4, 12, "Front"),
                    new FaceSegment( 8,  20,  4, 12, "Left"),
                    new FaceSegment( 12,  20,  4, 12, "Back"),

                    // 左足オーバーレイ
                    new FaceSegment(20,  16,  4,  4, "Top"),
                    new FaceSegment(24,  16,  4,  4, "Bot"),
                    new FaceSegment(16,  20,  4, 12, "Right"),
                    new FaceSegment(20,  20,  4, 12, "Front"),
                    new FaceSegment(24,  20,  4, 12, "Left"),
                    new FaceSegment(28,  20,  4, 12, "Back"),
            },
            new FaceSegment[]{
                    // 右足スキン
                    new FaceSegment( 4,  0,  4,  4, "Top"),
                    new FaceSegment( 8,  0,  4,  4, "Bot"),
                    new FaceSegment( 0,  4,  4, 12, "Right"),
                    new FaceSegment( 4,  4,  4, 12, "Front"),
                    new FaceSegment( 8,  4,  4, 12, "Left"),
                    new FaceSegment( 12,  4,  4, 12, "Back"),

                    // 左足スキン
                    new FaceSegment(20,  0,  4,  4, "Top"),
                    new FaceSegment(24,  0,  4,  4, "Bot"),
                    new FaceSegment(16,  4,  4, 12, "Right"),
                    new FaceSegment(20,  4,  4, 12, "Front"),
                    new FaceSegment(24,  4,  4, 12, "Left"),
                    new FaceSegment(28,  4,  4, 12, "Back"),

                    // 右足オーバーレイ
                    new FaceSegment( 4,  16,  4,  4, "Top"),
                    new FaceSegment( 8,  16,  4,  4, "Bot"),
                    new FaceSegment( 0,  20,  4, 12, "Right"),
                    new FaceSegment( 4,  20,  4, 12, "Front"),
                    new FaceSegment( 8,  20,  4, 12, "Left"),
                    new FaceSegment( 12,  20,  4, 12, "Back"),

                    // 左足オーバーレイ
                    new FaceSegment(20,  16,  4,  4, "Top"),
                    new FaceSegment(24,  16,  4,  4, "Bot"),
                    new FaceSegment(16,  20,  4, 12, "Right"),
                    new FaceSegment(20,  20,  4, 12, "Front"),
                    new FaceSegment(24,  20,  4, 12, "Left"),
                    new FaceSegment(28,  20,  4, 12, "Back"),
            }
    ),

    FEET(
            new CanvasSegment[]{
                    new CanvasSegment(0,  0, 8, 32, 4,  4), // 右靴底
                    new CanvasSegment(0,  4, 0, 42, 16, 6), // 右靴側面下6
                    new CanvasSegment(16, 0, 8, 48, 4,  4), // 左靴底
                    new CanvasSegment(16, 4, 0, 58, 16, 6)  // 左靴側面下6
            },
            "feet",
            new FaceSegment[]{
                    new FaceSegment( 0,  0,  4,  4, "RSole"),
                    new FaceSegment( 0,  4, 16,  6, "RSide"),
                    new FaceSegment(16,  0,  4,  4, "LSole"),
                    new FaceSegment(16,  4, 16,  6, "LSide")
            },
            new FaceSegment[]{
                    new FaceSegment( 0,  0,  4,  4, "RSole"),
                    new FaceSegment( 0,  4, 16,  6, "RSide"),
                    new FaceSegment(16,  0,  4,  4, "LSole"),
                    new FaceSegment(16,  4, 16,  6, "LSide")
            }
    );

    public record CanvasSegment(
        int canvasX,
        int canvasY,
        int uvX,
        int uvY,
        int w,
        int h
    ) {}
    public record FaceSegment(
        int canvasX,
        int canvasY,
        int w,
        int h,
        String label
    ) {}

    private final CanvasSegment[] segments;
    private final FaceSegment[] faceSegments;
    private final FaceSegment[] slimSegments;
    private final int canvasW;
    private final int canvasH;
    private final String type;

    PatternType(CanvasSegment[] segments, String type, FaceSegment[] faceSegments, FaceSegment[] slimSegments) {
        this.segments = segments;
        this.faceSegments = faceSegments;
        this.slimSegments = slimSegments;
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
    public FaceSegment[] getFaceSegments() { return faceSegments; }
    public FaceSegment[] getSlimSegments() { return slimSegments; }
    public int getCanvasW() { return canvasW; }
    public int getCanvasH() { return canvasH; }
    public String getType() { return type; }

    public int getTexW()          { return canvasW; }
    public int getTexH()          { return canvasH; }
    public int getUvX()           { return segments[0].uvX(); }
    public int getUvY()           { return segments[0].uvY(); }
    public int[] getTextureSize() { return new int[]{canvasW, canvasH}; }
}