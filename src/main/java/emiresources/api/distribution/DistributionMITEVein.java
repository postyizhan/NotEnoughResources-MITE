package emiresources.api.distribution;

import emiresources.util.FastRandom;

/**
 * The height distribution of a MITE ore, obtained by replaying MITE's own generation code.
 * <p>
 * PLAN.md originally called for an analytic approximation of the vein density. Replaying the real
 * algorithm turned out to be both simpler and considerably more accurate, because the vein size in
 * {@code WorldGenMinable.generate} passes through several stages that have no tidy closed form:
 * a geometric product of random factors, a clamp at 4x, a depth-dependent rescale, a coin flip that
 * either doubles or cancels small veins, and special cases for one- and two-block veins. Each
 * sample here walks that exact sequence, so the percentages on the graph are real rather than
 * estimated.
 * <p>
 * Two approximations remain, both of which only matter near the very bottom and top of the world:
 * <ul>
 *     <li>The world is assumed to be solid stone. In practice a vein attempt is abandoned when it
 *         starts in air, water, or an existing vein, so true densities are marginally lower.</li>
 *     <li>Blocks that a vein would place outside {@code [0, 255]} are dropped, whereas MITE would
 *         retry another direction and place them elsewhere.</li>
 * </ul>
 */
public class DistributionMITEVein extends DistributionBase {
    private static final int SAMPLES = 12000;
    private static final int MAX_Y = DistributionHelpers.HEIGHT - 1;

    /**
     * @param oreBlockId    the ore's block id, used only to give each ore its own sampling stream
     * @param minY          the ore's {@code getMinVeinHeight}
     * @param maxY          the ore's {@code getMaxVeinHeight}
     * @param veinSize      {@code WorldGenMinable.numberOfBlocks}, before any scaling
     * @param veinsPerChunk expected vein attempts per chunk, i.e. the {@code genMinable} count after
     *                      dimension multipliers and divided by ten
     * @param deep          the {@code vein_size_increases_with_depth} flag {@code genMinable} passes
     * @param shape         which branch of {@code getRandomVeinHeight} applies
     */
    public DistributionMITEVein(int oreBlockId, int minY, int maxY, int veinSize,
                                float veinsPerChunk, boolean deep, VeinShape shape) {
        super(simulate(oreBlockId, minY, maxY, veinSize, veinsPerChunk, deep, shape));
        this.bestHeight = DistributionHelpers.calculateMeanLevel(getDistribution());
    }

    private static float[] simulate(int oreBlockId, int minY, int maxY, int veinSize,
                                    float veinsPerChunk, boolean deep, VeinShape shape) {
        double[] blocksPerLevel = new double[DistributionHelpers.HEIGHT];
        FastRandom rand = new FastRandom(0x9E3779B9L * 31L + oreBlockId);

        for (int sample = 0; sample < SAMPLES; sample++) {
            int startY = shape.sampleY(rand, minY, maxY);
            if (startY < 0 || startY > MAX_Y) {
                // genMinable skips negative heights outright; anything above the world cannot be built.
                continue;
            }
            int blocks = rollVeinSize(rand, startY, minY, maxY, veinSize, deep);
            if (blocks < 1) {
                continue;
            }
            float[] profile = VeinProfiles.get(blocks);
            for (int i = 0; i < profile.length; i++) {
                float expected = profile[i];
                if (expected == 0.0F) {
                    continue;
                }
                int y = startY + i - VeinProfiles.ORIGIN;
                if (y >= 0 && y <= MAX_Y) {
                    blocksPerLevel[y] += expected;
                }
            }
        }

        // blocksPerLevel/SAMPLES is the ore blocks one attempt leaves at that height; scaling by the
        // attempts per chunk and dividing by the chunk's 256 columns turns it into the chance that a
        // given block at that height is this ore.
        float[] chances = new float[DistributionHelpers.HEIGHT];
        double factor = veinsPerChunk / (SAMPLES * 256.0);
        for (int y = 0; y < chances.length; y++) {
            chances[y] = (float) (blocksPerLevel[y] * factor);
        }
        return chances;
    }

    /**
     * Replays the vein size calculation from {@code WorldGenMinable.generate}.
     *
     * @return the number of blocks to grow, or {@code 0} if the attempt is abandoned
     */
    private static int rollVeinSize(FastRandom rand, int y, int minY, int maxY, int veinSize, boolean deep) {
        float scale = 1.0F;
        while (rand.nextInt(2) == 0) {
            scale = (float) ((double) scale * ((double) rand.nextFloat() * 0.6D + 0.699999988079071D));
        }
        scale = Math.min(scale, 4.0F);

        if (deep) {
            int range = maxY - minY;
            if (range > 16) {
                float relativeHeight = ((float) y - (float) minY) / (float) range;
                // 1.5x at the bottom of the band down to 0.5x at the top.
                scale *= 1.0F - relativeHeight + 0.5F;
            }
        }

        if ((float) veinSize * scale <= 3.0F && rand.nextInt(2) == 0) {
            if (rand.nextInt(2) != 0) {
                return 0;
            }
            scale *= 2.0F;
        }

        int blocks = (int) ((float) veinSize * scale);
        if (blocks < 1) {
            return 0;
        }
        if (blocks == 1) {
            return rand.nextInt(3) != 0 ? 0 : 3;
        }
        if (blocks == 2) {
            return rand.nextInt(3) == 0 ? 0 : 3;
        }
        return Math.min(blocks, VeinProfiles.MAX_VEIN);
    }
}
