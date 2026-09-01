package neresources.api.distribution;

/**
 * A flat band of ore, optionally with ramps at both ends to account for veins spilling past
 * the height limits. Suitable for mods that scatter a fixed number of same-sized veins.
 */
public class DistributionSquare extends DistributionBase {
    /**
     * Pure square distribution.
     *
     * @param minY   first occurrence
     * @param maxY   last occurrence
     * @param chance chance at every height in between
     */
    public DistributionSquare(int minY, int maxY, float chance) {
        super(DistributionHelpers.getSquareDistribution(minY, maxY, chance));
        this.bestHeight = (minY + maxY) / 2;
    }

    /**
     * Rounded square distribution.
     *
     * @param min0   start of the ramp up
     * @param minY   end of the ramp up
     * @param maxY   start of the ramp down
     * @param max0   end of the ramp down
     * @param chance chance on the plateau
     */
    public DistributionSquare(int min0, int minY, int maxY, int max0, float chance) {
        super(DistributionHelpers.getRoundedSquareDistribution(
                Math.max(min0, 0), Math.max(minY, 0),
                Math.min(maxY, DistributionHelpers.HEIGHT - 1), Math.min(max0, DistributionHelpers.HEIGHT - 1),
                chance));
        this.bestHeight = DistributionHelpers.calculateMeanLevel(getDistribution());
    }

    /**
     * Rounded square distribution derived from vein statistics.
     *
     * @param veinCount veins per chunk
     * @param veinSize  blocks per vein
     * @param minY      lowest Y a vein may start at
     * @param maxY      highest Y a vein may start at
     */
    public DistributionSquare(float veinCount, int veinSize, int minY, int maxY) {
        this(minY - veinSize / 2, minY, maxY, maxY + veinSize / 2,
                DistributionHelpers.calculateChance(veinCount, veinSize, minY, maxY));
    }
}
