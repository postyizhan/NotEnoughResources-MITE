package emiresources.api.distribution;

import java.util.Random;

/**
 * The height distributions {@code WorldGenMinable.getRandomVeinHeight} can pick a vein start from.
 * <p>
 * MITE draws a normalised factor {@code f} and maps it onto the ore's height band with
 * {@code y = min + (int)(f * (max - min + 1))}. Every shape below reproduces one of the branches
 * of that method; the rejection loops are written as their closed forms, which give the same
 * distribution with fewer draws:
 * <ul>
 *     <li>{@code do f = rand(); while (f <= rand());} accepts proportionally to {@code f},
 *         i.e. {@code max} of two uniforms.</li>
 *     <li>{@code do f = rand(); while (f >= rand());} accepts proportionally to {@code 1 - f},
 *         i.e. {@code min} of two uniforms.</li>
 * </ul>
 */
public enum VeinShape {
    /** {@code f = max(u1, u2)}, density {@code 2f} — biased towards the top of the band. */
    ASCENDING {
        @Override
        public int sampleY(Random rand, int minY, int maxY) {
            return map(Math.max(rand.nextFloat(), rand.nextFloat()), minY, maxY);
        }
    },
    /** {@code f = min(u1, u2)}, density {@code 2(1 - f)} — biased towards the bottom of the band. */
    DESCENDING {
        @Override
        public int sampleY(Random rand, int minY, int maxY) {
            return map(Math.min(rand.nextFloat(), rand.nextFloat()), minY, maxY);
        }
    },
    /** {@code f = (u1 + u2) / 2}, a triangle centred on the middle of the band. */
    TRIANGULAR {
        @Override
        public int sampleY(Random rand, int minY, int maxY) {
            return map((rand.nextFloat() + rand.nextFloat()) / 2.0F, minY, maxY);
        }
    },
    /** Copper only: half the time uniform over {@code [0.4, 1]}, half the time {@link #DESCENDING}. */
    COPPER_MIX {
        @Override
        public int sampleY(Random rand, int minY, int maxY) {
            float f = rand.nextInt(2) == 0
                    ? rand.nextFloat() * 0.6F + 0.4F
                    : Math.min(rand.nextFloat(), rand.nextFloat());
            return map(f, minY, maxY);
        }
    },
    /** Underworld, non-ore blocks (gravel, silverfish): uniform over the whole column. */
    UNDERWORLD_UNIFORM {
        @Override
        public int sampleY(Random rand, int minY, int maxY) {
            return rand.nextInt(256);
        }
    },
    /**
     * Underworld {@code BlockOre}: 75% uniform over {@code [0, 16 + underworld_y_offset)},
     * otherwise uniform over the whole column. {@code underworld_y_offset} is always 120.
     */
    UNDERWORLD_ORE {
        @Override
        public int sampleY(Random rand, int minY, int maxY) {
            return rand.nextFloat() < 0.75F ? rand.nextInt(136) : rand.nextInt(256);
        }
    },
    /** Underworld adamantite: always uniform over {@code [0, 16 + underworld_y_offset)}. */
    UNDERWORLD_ADAMANTITE {
        @Override
        public int sampleY(Random rand, int minY, int maxY) {
            return rand.nextInt(136);
        }
    };

    /** Applies MITE's {@code y = min + (int)(f * (max - min + 1))}. */
    protected static int map(float f, int minY, int maxY) {
        return minY + (int) (f * (maxY - minY + 1));
    }

    /**
     * @param minY the ore's {@code getMinVeinHeight}
     * @param maxY the ore's {@code getMaxVeinHeight}
     * @return the Y a vein starts growing from
     */
    public abstract int sampleY(Random rand, int minY, int maxY);
}
