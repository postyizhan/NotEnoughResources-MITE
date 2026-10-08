package emiresources.api.restriction;

import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.ArrayList;
import java.util.List;

/** Where an ore generates: a dimension restriction plus an optional biome restriction. */
public class Restriction {
    public static final Restriction OVERWORLD = new Restriction(DimensionRestriction.OVERWORLD);
    public static final Restriction NETHER = new Restriction(DimensionRestriction.NETHER);
    public static final Restriction END = new Restriction(DimensionRestriction.END);
    public static final Restriction UNDERWORLD = new Restriction(DimensionRestriction.UNDERWORLD);
    /** No restriction at all — the ore generates everywhere. */
    public static final Restriction ANY = new Restriction();

    private final BiomeRestriction biomeRestriction;
    private final DimensionRestriction dimensionRestriction;

    public Restriction() {
        this(BiomeRestriction.NONE, DimensionRestriction.NONE);
    }

    public Restriction(DimensionRestriction dimensionRestriction) {
        this(BiomeRestriction.NONE, dimensionRestriction);
    }

    public Restriction(BiomeRestriction biomeRestriction) {
        this(biomeRestriction, DimensionRestriction.NONE);
    }

    public Restriction(BiomeRestriction biomeRestriction, DimensionRestriction dimensionRestriction) {
        this.biomeRestriction = biomeRestriction;
        this.dimensionRestriction = dimensionRestriction;
    }

    /** @return the dimension part, for grouping entries by dimension. */
    public DimensionRestriction getDimensionRestriction() {
        return dimensionRestriction;
    }

    /** @return true if this ore can be found in the given dimension. */
    public boolean permitsDimension(int dimensionId) {
        return dimensionRestriction.permits(dimensionId);
    }

    /** @return the tooltip lines describing where the ore generates. */
    public List<String> getStringList() {
        List<String> result = new ArrayList<String>();
        result.add(StatCollector.translateToLocal("emir.ore.dimensions") + ":");
        result.addAll(dimensionRestriction.toStringList());
        List<String> biomes = biomeRestriction.toStringList();
        if (!biomes.isEmpty()) {
            result.add(StatCollector.translateToLocal("emir.ore.biomes") + ":");
            result.addAll(biomes);
        }
        return result;
    }

    /**
     * @return true when the two restrictions describe the same places, so their ores can share one
     *         graph.
     */
    public boolean isMergeable(Restriction other) {
        return biomeRestriction.isMergeable(other.biomeRestriction)
                && dimensionRestriction.isMergeable(other.dimensionRestriction);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Restriction)) {
            return false;
        }
        Restriction other = (Restriction) obj;
        return other.biomeRestriction.equals(biomeRestriction)
                && other.dimensionRestriction.equals(dimensionRestriction);
    }

    @Override
    public int hashCode() {
        return biomeRestriction.hashCode() * 31 + dimensionRestriction.hashCode();
    }

    @Override
    public String toString() {
        return dimensionRestriction + ", " + biomeRestriction;
    }

    /** Convenience for the common single-dimension case. */
    public static Restriction forDimension(int dimensionId) {
        switch (dimensionId) {
            case World.DIMENSION_ID_OVERWORLD:
                return OVERWORLD;
            case World.DIMENSION_ID_NETHER:
                return NETHER;
            case World.DIMENSION_ID_THE_END:
                return END;
            case World.DIMENSION_ID_UNDERWORLD:
                return UNDERWORLD;
            default:
                return new Restriction(new DimensionRestriction(dimensionId));
        }
    }
}
