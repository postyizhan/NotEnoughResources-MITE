package emiresources.api.distribution;

/**
 * Array maths for building and combining 256-entry height distributions.
 * Ported from NotEnoughResources; the NBT helpers were dropped because MITE has no IMC channel.
 */
public final class DistributionHelpers {
    /** Distributions always span the full world height. */
    public static final int HEIGHT = 256;

    private DistributionHelpers() {
    }

    /**
     * @param midY      the middle (and top) of the triangle
     * @param range     length of each side
     * @param maxChance chance at the top
     */
    public static float[] getTriangularDistribution(int midY, int range, float maxChance) {
        return getTriangularDistribution(midY - range, range, range, maxChance);
    }

    public static float[] getTriangularDistribution(int minY, int rand1, int rand2, float maxChance) {
        float[] triangle = new float[rand1 + rand2 + 1];
        float modChance = maxChance / Math.min(rand1, rand2);
        for (int i = 0; i < rand1; i++) {
            for (int j = 0; j < rand2; j++) {
                triangle[i + j] += modChance;
            }
        }
        float[] result = new float[HEIGHT];
        for (int i = 0; i < triangle.length; i++) {
            int mapToPos = i + minY;
            if (mapToPos < 0) {
                continue;
            }
            if (mapToPos == result.length) {
                break;
            }
            result[mapToPos] = triangle[i];
        }
        return result;
    }

    /**
     * @param minY   first occurrence
     * @param maxY   last occurrence
     * @param chance the flat chance between them
     */
    public static float[] getSquareDistribution(int minY, int maxY, float chance) {
        float[] result = new float[HEIGHT];
        for (int i = Math.max(minY, 0); i <= Math.min(maxY, HEIGHT - 1); i++) {
            result[i] = chance;
        }
        return result;
    }

    /**
     * @param min0   start of the ramp up
     * @param minY   end of the ramp up
     * @param maxY   start of the ramp down
     * @param max0   end of the ramp down
     * @param chance the chance on the plateau
     */
    public static float[] getRoundedSquareDistribution(int min0, int minY, int maxY, int max0, float chance) {
        float[] result = new float[HEIGHT];
        addDistribution(result, getRampDistribution(min0, minY, chance), min0);
        addDistribution(result, getSquareDistribution(minY, maxY, chance));
        addDistribution(result, getRampDistribution(max0, maxY, chance), maxY);
        return result;
    }

    /**
     * @return an array of {@code |maxY - minY| + 1} floats ramping up to {@code maxChance}
     */
    public static float[] getRampDistribution(int minY, int maxY, float maxChance) {
        if (minY == maxY) {
            return new float[0];
        }
        if (minY > maxY) {
            return reverse(getRampDistribution(maxY, minY, maxChance));
        }
        int range = maxY - minY;
        float[] result = new float[range + 1];
        for (int i = 0; i < range; i++) {
            result[i] = (maxChance * (float) i) / range;
        }
        return result;
    }

    /** Adds {@code add} into {@code base} in place and returns {@code base}. */
    public static float[] addDistribution(float[] base, float[] add) {
        return addDistribution(base, add, 0);
    }

    /** Adds {@code add} into {@code base} starting at {@code offset}, in place. */
    public static float[] addDistribution(float[] base, float[] add, int offset) {
        int addCount = 0;
        for (int i = Math.max(offset, 0); i < Math.min(base.length, add.length + offset); i++) {
            base[i] += add[addCount++];
        }
        return base;
    }

    public static float[] reverse(float[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[array.length - 1 - i] = array[i];
        }
        return result;
    }

    /**
     * Finds the height where the ore is densest, measured over a 4-block window so a single
     * spiky sample does not win.
     *
     * @return the Y with the highest chance of yielding the ore
     */
    public static int calculateMeanLevel(float[] distribution) {
        float adjacent = 0;
        float maxAdjacent = 0;
        int consecutive = 0;
        int mid = 0;
        for (int i = 0; i < 4 && i < distribution.length; i++) {
            adjacent += distribution[i];
        }
        for (int i = 0; i < distribution.length - 4; i++) {
            adjacent -= distribution[i] - distribution[i + 4];
            if (adjacent > maxAdjacent) {
                mid = i + 2;
                maxAdjacent = adjacent + 0.00001F;
                consecutive = 0;
            } else if (adjacent > maxAdjacent - 0.00002F) {
                consecutive++;
            } else {
                mid += consecutive / 2;
                consecutive = 0;
            }
        }
        return mid;
    }

    public static float[] multiplyArray(float[] array, float num) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] * num;
        }
        return result;
    }

    public static float sum(float[] distribution) {
        float result = 0;
        for (float val : distribution) {
            result += val;
        }
        return result;
    }

    /**
     * @param veinCount veins per chunk
     * @param veinSize  blocks per vein
     * @return the chance that a given block inside the band is part of a vein
     */
    public static float calculateChance(float veinCount, int veinSize, int minY, int maxY) {
        return (veinCount * veinSize) / ((maxY - minY + 1) * 256F);
    }
}
