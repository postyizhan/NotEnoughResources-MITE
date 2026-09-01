package neresources.util;

import java.util.Random;

/**
 * A non-thread-safe {@link Random} for the ore distribution sampling.
 * <p>
 * {@link Random} does a compare-and-set on an {@code AtomicLong} for every single draw, which
 * dominates the runtime of the vein simulation (tens of millions of draws). This subclass swaps in
 * a plain xorshift64* so the draws become a few ALU ops. The sampler only ever averages over
 * samples, so it does not need to reproduce MITE's exact random stream — just the distributions.
 */
public final class FastRandom extends Random {
    private static final long serialVersionUID = 1L;

    private long state;

    public FastRandom(long seed) {
        super(0L);
        setSeed(seed);
    }

    @Override
    public void setSeed(long seed) {
        // Also called from Random's constructor, before our own seeding; both paths are fine.
        long z = seed + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        this.state = z == 0L ? 0x9E3779B97F4A7C15L : z;
    }

    @Override
    protected int next(int bits) {
        long x = state;
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        state = x;
        return (int) ((x * 0x2545F4914F6CDD1DL) >>> (64 - bits));
    }
}
