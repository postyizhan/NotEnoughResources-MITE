package emiresources.api.util;

import net.minecraft.StatCollector;

/**
 * The light level a mob needs in order to spawn.
 * <p>
 * MITE's checks are probabilistic rather than hard cut-offs, so the numbers here are the point at
 * which spawning becomes impossible. {@code EntityMob.isValidLightLevel} ends in
 * {@code blockLight <= rand.nextInt(isUnderOpenSky() ? 8 : 5)}: at light 0 a mob always passes, and
 * the odds shrink as the light rises until the limit is reached.
 */
public class LightLevel {
    /** Spawns regardless of light — golems, silverfish, blazes, elementals. */
    public static final LightLevel any = new LightLevel(-1, Relative.ABOVE);
    /** Bats: {@code light <= rand.nextInt(4)}, and only below y=63 in the overworld. */
    public static final LightLevel bat = new LightLevel(4, Relative.BELOW);
    /** Hostile mobs: below 5 in an enclosed space, below 8 under open sky. */
    public static final LightLevel hostile = new LightLevel(5, Relative.BELOW, 8);
    /**
     * Animals need grass underneath and a light level strictly above 8
     * ({@code EntityAnimal.getCanSpawnHere} checks {@code getFullBlockLightValue > 8}).
     */
    public static final LightLevel animal = new LightLevel(8, Relative.ABOVE);

    private final int lightLevel;
    private final Relative relative;
    /** The limit that applies under open sky, or -1 when it does not differ. */
    private final int openSkyLightLevel;

    public LightLevel(int level, Relative relative) {
        this(level, relative, -1);
    }

    public LightLevel(int level, Relative relative, int openSkyLightLevel) {
        this.lightLevel = level;
        this.relative = relative;
        this.openSkyLightLevel = openSkyLightLevel;
    }

    public int getLightLevel() {
        return lightLevel;
    }

    /** @return the line shown on the mob page, e.g. {@code "Light level: below 5 (below 8 under open sky)"}. */
    @Override
    public String toString() {
        String base = StatCollector.translateToLocal("emir.mob.lightLevel");
        if (lightLevel < 0) {
            return base + ": " + StatCollector.translateToLocal("emir.any");
        }
        String limit = StatCollector.translateToLocal(relative.key) + " " + lightLevel;
        if (openSkyLightLevel < 0) {
            return base + ": " + limit;
        }
        String openSkyLimit = StatCollector.translateToLocal(relative.key) + " " + openSkyLightLevel;
        return base + ": " + limit + " ("
                + StatCollector.translateToLocalFormatted("emir.mob.lightLevel.openSky", openSkyLimit) + ")";
    }

    public enum Relative {
        ABOVE("emir.above"),
        BELOW("emir.below");

        private final String key;

        Relative(String key) {
            this.key = key;
        }
    }
}
