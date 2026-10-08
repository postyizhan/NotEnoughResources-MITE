package emiresources.api.distribution;

/**
 * A 256-entry array of per-Y chances that a block at that height is the registered ore,
 * plus the height at which the ore is densest.
 */
public abstract class DistributionBase {
    private final float[] distribution;
    protected int bestHeight;

    public DistributionBase(float[] distribution) {
        this.distribution = distribution;
    }

    /** @return the raw chances, indexed by Y. Never null, always {@value DistributionHelpers#HEIGHT} long. */
    public float[] getDistribution() {
        return distribution;
    }

    /** @return the Y with the best chance of finding the ore. */
    public int getBestHeight() {
        return bestHeight;
    }
}
