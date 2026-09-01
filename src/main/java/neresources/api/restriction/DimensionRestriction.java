package neresources.api.restriction;

import net.minecraft.World;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** Which dimensions an ore generates in. */
public class DimensionRestriction {
    public static final DimensionRestriction NONE = new DimensionRestriction();
    public static final DimensionRestriction OVERWORLD = new DimensionRestriction(World.DIMENSION_ID_OVERWORLD);
    public static final DimensionRestriction NETHER = new DimensionRestriction(World.DIMENSION_ID_NETHER);
    public static final DimensionRestriction END = new DimensionRestriction(World.DIMENSION_ID_THE_END);
    public static final DimensionRestriction UNDERWORLD = new DimensionRestriction(World.DIMENSION_ID_UNDERWORLD);

    private final Set<Integer> dimensions = new LinkedHashSet<Integer>();
    private final RestrictionType type;

    public DimensionRestriction() {
        this.type = RestrictionType.NONE;
    }

    public DimensionRestriction(int... dimensionIds) {
        this(RestrictionType.WHITELIST, dimensionIds);
    }

    public DimensionRestriction(RestrictionType type, int... dimensionIds) {
        this.type = type;
        for (int id : dimensionIds) {
            this.dimensions.add(id);
        }
    }

    /** @return the dimension ids this restriction permits, sorted. */
    public Set<Integer> getValidDimensions() {
        Set<Integer> result = new TreeSet<Integer>();
        for (int dimension : DimensionRegistry.getDimensions()) {
            if (permits(dimension)) {
                result.add(dimension);
            }
        }
        return result;
    }

    public boolean permits(int dimensionId) {
        switch (type) {
            case WHITELIST:
                return dimensions.contains(dimensionId);
            case BLACKLIST:
                return !dimensions.contains(dimensionId);
            default:
                return true;
        }
    }

    /** @return one localised dimension name per line, for the ore tooltip. */
    public List<String> toStringList() {
        List<String> result = new ArrayList<String>();
        for (int dimension : getValidDimensions()) {
            result.add("  " + DimensionRegistry.getDimensionName(dimension));
        }
        return result;
    }

    /** @return true when {@code other} permits everything this one does, so the two can share a graph. */
    public boolean isMergeable(DimensionRestriction other) {
        return other.getValidDimensions().containsAll(getValidDimensions());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DimensionRestriction)) {
            return false;
        }
        DimensionRestriction other = (DimensionRestriction) obj;
        return other.getValidDimensions().equals(getValidDimensions());
    }

    @Override
    public int hashCode() {
        return getValidDimensions().hashCode();
    }

    @Override
    public String toString() {
        return "Dimensions: " + getValidDimensions();
    }
}
