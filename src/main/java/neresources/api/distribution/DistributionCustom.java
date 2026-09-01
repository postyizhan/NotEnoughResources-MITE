package neresources.api.distribution;

import java.util.Arrays;

/**
 * A distribution supplied verbatim by the caller, for generation schemes the built-in shapes
 * cannot express.
 */
public class DistributionCustom extends DistributionBase {
    /** Derives the best height from the array itself. */
    public DistributionCustom(float[] distribution) {
        super(fit(distribution));
        this.bestHeight = DistributionHelpers.calculateMeanLevel(getDistribution());
    }

    public DistributionCustom(float[] distribution, int bestHeight) {
        super(fit(distribution));
        this.bestHeight = bestHeight;
    }

    /** Pads or truncates to {@value DistributionHelpers#HEIGHT} so consumers can index freely. */
    private static float[] fit(float[] distribution) {
        if (distribution == null) {
            return new float[DistributionHelpers.HEIGHT];
        }
        if (distribution.length == DistributionHelpers.HEIGHT) {
            return distribution;
        }
        return Arrays.copyOf(distribution, DistributionHelpers.HEIGHT);
    }
}
