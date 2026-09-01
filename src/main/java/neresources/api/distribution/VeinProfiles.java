package neresources.api.distribution;

import neresources.util.FastRandom;

/**
 * Vertical shape of a MITE ore vein, as a function of its block count.
 * <p>
 * {@code WorldGenMinable.growVein} grows a vein by walking one block at a time along a randomly
 * chosen axis, up to 16 tries per block, refusing to reuse a position. The resulting silhouette
 * depends only on how many blocks the vein was given — not on where it started or which ore it is —
 * so the walk is simulated once per size here and reused by every ore.
 * <p>
 * A profile records the expected number of blocks placed at each vertical offset from the vein's
 * origin. Summing a profile gives the expected blocks actually placed, which is slightly below the
 * requested count because the walk can box itself in.
 */
public final class VeinProfiles {
    /** MITE clamps veins to 32 blocks, so a vein can never drift further than 32 blocks vertically. */
    public static final int MAX_VEIN = 32;
    /** Index of {@code deltaY == 0} inside a profile. */
    public static final int ORIGIN = MAX_VEIN;
    /** Profile length: offsets {@code -32..32}. */
    public static final int LENGTH = MAX_VEIN * 2 + 1;

    private static final int SAMPLES = 1500;
    private static final int EXTENT = MAX_VEIN * 2 + 1;
    private static final float[][] CACHE = new float[MAX_VEIN + 1][];

    private VeinProfiles() {
    }

    /**
     * @param veinSize blocks the vein was told to grow, {@code 1..32}
     * @return expected blocks per vertical offset, indexed by {@code deltaY + }{@value #ORIGIN}.
     *         The array is shared and must not be modified.
     */
    public static synchronized float[] get(int veinSize) {
        int n = Math.max(1, Math.min(veinSize, MAX_VEIN));
        float[] cached = CACHE[n];
        if (cached == null) {
            cached = new Walker().sample(n);
            CACHE[n] = cached;
        }
        return cached;
    }

    /**
     * One reusable simulation workspace. Occupancy lives in a flat array covering every position the
     * walk can reach; only the cells actually visited are reset between samples, so a sample costs
     * time proportional to the vein size rather than to the workspace.
     */
    private static final class Walker {
        private final boolean[] occupied = new boolean[EXTENT * EXTENT * EXTENT];
        private final int[] touched = new int[MAX_VEIN + 1];
        private final long[] perOffset = new long[LENGTH];
        private final FastRandom rand = new FastRandom(0x4E45525F56454956L);
        private int touchedCount;

        float[] sample(int veinSize) {
            for (int i = 0; i < SAMPLES; i++) {
                touchedCount = 0;
                grow(veinSize, 0, 0, 0);
                for (int t = 0; t < touchedCount; t++) {
                    occupied[touched[t]] = false;
                }
            }
            float[] profile = new float[LENGTH];
            for (int i = 0; i < LENGTH; i++) {
                profile[i] = (float) perOffset[i] / SAMPLES;
                perOffset[i] = 0L;
            }
            return profile;
        }

        /** Mirrors {@code WorldGenMinable.growVein} for a world made entirely of stone. */
        private int grow(int remaining, int x, int y, int z) {
            if (remaining < 1) {
                return 0;
            }
            int index = index(x, y, z);
            if (index < 0 || occupied[index]) {
                // Outside the reachable box, or already ore: getBlockId != blockToReplace.
                return 0;
            }
            occupied[index] = true;
            touched[touchedCount++] = index;
            perOffset[y + ORIGIN]++;

            int placed = 1;
            for (int attempt = 0; attempt < 16; attempt++) {
                int dx = 0;
                int dy = 0;
                int dz = 0;
                int axis = rand.nextInt(3);
                if (axis == 0) {
                    dx = rand.nextInt(2) == 0 ? -1 : 1;
                } else if (axis == 1) {
                    dy = rand.nextInt(2) == 0 ? -1 : 1;
                } else {
                    dz = rand.nextInt(2) == 0 ? -1 : 1;
                }
                placed += grow(remaining - placed, x + dx, y + dy, z + dz);
                if (placed == remaining) {
                    break;
                }
            }
            return placed;
        }

        private static int index(int x, int y, int z) {
            int ix = x + MAX_VEIN;
            int iy = y + MAX_VEIN;
            int iz = z + MAX_VEIN;
            if (ix < 0 || ix >= EXTENT || iy < 0 || iy >= EXTENT || iz < 0 || iz >= EXTENT) {
                return -1;
            }
            return (ix * EXTENT + iy) * EXTENT + iz;
        }
    }
}
