package com.tailormade.tailor.utils;

public final class DyeCostCalculator {
    public static final int DYE_TANK_PER_ITEM = 100;
    public static final double COST_SCALE = 40.0;

    private DyeCostCalculator() {}

    public record DyeCost(int red, int green, int blue) {
        public static final DyeCost ZERO = new DyeCost(0, 0, 0);
        public boolean canAfford(int tankR, int tankG, int tankB) {
            return tankR >= red && tankG >= green && tankB >= blue;
        }
    }

    public static DyeCost calculate(int[] pixels) {
        if (pixels == null || pixels.length == 0) return DyeCost.ZERO;

        long sumR = 0, sumG = 0, sumB = 0;
        int total = pixels.length;

        for (int argb : pixels) {
            int alpha = (argb >> 24) & 0xFF;
            if (alpha == 0) continue;

            sumR += (argb >> 16) & 0xFF;
            sumG += (argb >>  8) & 0xFF;
            sumB +=  argb & 0xFF;
        }

        double divisor = 255.0 * total;

        int costR = (int) Math.ceil(sumR / divisor * COST_SCALE);
        int costG = (int) Math.ceil(sumG / divisor * COST_SCALE);
        int costB = (int) Math.ceil(sumB / divisor * COST_SCALE);

        return new DyeCost(costR, costG, costB);
    }

    public static int[] itemsNeeded(int tankR, int tankG, int tankB, DyeCost cost) {
        int needR = Math.max(0, (int) Math.ceil((cost.red()   - tankR) / (double) DYE_TANK_PER_ITEM));
        int needG = Math.max(0, (int) Math.ceil((cost.green() - tankG) / (double) DYE_TANK_PER_ITEM));
        int needB = Math.max(0, (int) Math.ceil((cost.blue()  - tankB) / (double) DYE_TANK_PER_ITEM));
        return new int[]{needR, needG, needB};
    }
}
