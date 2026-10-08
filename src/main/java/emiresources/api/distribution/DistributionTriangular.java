package emiresources.api.distribution;

/**
 * A triangle peaking at {@code midY} — the shape you get when a vein height is drawn as the
 * average of two uniform rolls.
 */
public class DistributionTriangular extends DistributionBase {
    /**
     * @param midY      top of the triangle
     * @param range     length of each side
     * @param maxChance chance at the top
     */
    public DistributionTriangular(int midY, int range, float maxChance) {
        super(DistributionHelpers.getTriangularDistribution(midY, range, maxChance));
        this.bestHeight = midY;
    }
}
