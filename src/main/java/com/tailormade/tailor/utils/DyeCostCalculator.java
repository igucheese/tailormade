package com.tailormade.tailor.utils;

public final class DyeCostCalculator {

    /** 染料アイテム 1 個でタンクに補充される量 */
    public static final int DYE_TANK_PER_ITEM = 100;

    /**
     * 全ピクセルのそのチャンネル値が最大 (255) のときに消費するタンク量
     */
    public static final double COST_SCALE = 40.0;

    private DyeCostCalculator() {}

    // ---- 結果レコード -----------------------------------------
    public record DyeCost(int red, int green, int blue) {
        public static final DyeCost ZERO = new DyeCost(0, 0, 0);

        /** タンクの現在値で仕立て可能かチェック */
        public boolean canAfford(int tankR, int tankG, int tankB) {
            return tankR >= red && tankG >= green && tankB >= blue;
        }
    }

    // ---- 算出 -------------------------------------------------

    /**
     * PixelData の int[] (ARGB) から DyeCost を算出する。
     *
     * @param pixels ARGB 形式の int 配列（PixelData.pixels()）
     * @return 必要な RGB タンク消費量
     */
    public static DyeCost calculate(int[] pixels) {
        if (pixels == null || pixels.length == 0) return DyeCost.ZERO;

        long sumR = 0, sumG = 0, sumB = 0;
        int total = pixels.length;

        for (int argb : pixels) {
            int alpha = (argb >> 24) & 0xFF;
            if (alpha == 0) continue; // 透明ピクセルはコスト 0

            sumR += (argb >> 16) & 0xFF;
            sumG += (argb >>  8) & 0xFF;
            sumB +=  argb        & 0xFF;
        }

        double divisor = 255.0 * total;

        int costR = (int) Math.ceil(sumR / divisor * COST_SCALE);
        int costG = (int) Math.ceil(sumG / divisor * COST_SCALE);
        int costB = (int) Math.ceil(sumB / divisor * COST_SCALE);

        return new DyeCost(costR, costG, costB);
    }

    /**
     * タンク現在値と DyeCost から、それぞれのチャンネルで
     * 消費する染料アイテム数（0 以上）を返す。
     *
     * タンクが足りる限りアイテムを消費しないため、
     * タンク残量 = tankCurrent - cost が 0 未満になったとき繰り上げで消費する。
     *
     * 実際のアイテム消費は TailorMenu / ConfirmTailorPacketHandler が行う。
     *
     * @return int[3] = {needR, needG, needB} 追加で必要なアイテム数
     */
    public static int[] itemsNeeded(int tankR, int tankG, int tankB, DyeCost cost) {
        int needR = Math.max(0, (int) Math.ceil((cost.red()   - tankR) / (double) DYE_TANK_PER_ITEM));
        int needG = Math.max(0, (int) Math.ceil((cost.green() - tankG) / (double) DYE_TANK_PER_ITEM));
        int needB = Math.max(0, (int) Math.ceil((cost.blue()  - tankB) / (double) DYE_TANK_PER_ITEM));
        return new int[]{needR, needG, needB};
    }
}
