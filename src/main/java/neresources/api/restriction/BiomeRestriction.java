package neresources.api.restriction;

import net.minecraft.BiomeGenBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Which biomes an ore generates in.
 * <p>
 * MITE's own ore generation is biome-independent, so nothing in this mod restricts by biome; the
 * type exists for mods registering through {@code NEResourcesAPI}. Forge's {@code BiomeDictionary}
 * has no MITE equivalent, so biomes are listed explicitly.
 */
public class BiomeRestriction {
    public static final BiomeRestriction NONE = new BiomeRestriction();

    private final Set<BiomeGenBase> biomes = new LinkedHashSet<BiomeGenBase>();
    private final RestrictionType type;

    public BiomeRestriction() {
        this.type = RestrictionType.NONE;
    }

    public BiomeRestriction(BiomeGenBase... biomes) {
        this(RestrictionType.WHITELIST, biomes);
    }

    public BiomeRestriction(RestrictionType type, BiomeGenBase... biomes) {
        this.type = type;
        for (BiomeGenBase biome : biomes) {
            if (biome != null) {
                this.biomes.add(biome);
            }
        }
    }

    /** @return the biomes this restriction permits. Empty when unrestricted. */
    public Set<BiomeGenBase> getValidBiomes() {
        Set<BiomeGenBase> result = new LinkedHashSet<BiomeGenBase>();
        if (type == RestrictionType.NONE) {
            return result;
        }
        if (type == RestrictionType.WHITELIST) {
            result.addAll(biomes);
            return result;
        }
        for (BiomeGenBase biome : Arrays.asList(BiomeGenBase.biomeList)) {
            if (biome != null && !biomes.contains(biome)) {
                result.add(biome);
            }
        }
        return result;
    }

    /** @return one biome name per line, for the ore tooltip. Empty when unrestricted. */
    public List<String> toStringList() {
        List<String> result = new ArrayList<String>();
        for (BiomeGenBase biome : getValidBiomes()) {
            if (biome.biomeName != null && !biome.biomeName.isEmpty()) {
                result.add("  " + biome.biomeName);
            }
        }
        return result;
    }

    /** @return true when {@code other} permits everything this one does, so the two can share a graph. */
    public boolean isMergeable(BiomeRestriction other) {
        return other.type == RestrictionType.NONE
                || (this.type != RestrictionType.NONE && other.getValidBiomes().containsAll(getValidBiomes()));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BiomeRestriction)) {
            return false;
        }
        return ((BiomeRestriction) obj).getValidBiomes().equals(getValidBiomes());
    }

    @Override
    public int hashCode() {
        return getValidBiomes().hashCode();
    }

    @Override
    public String toString() {
        return "Biomes: " + type + (type == RestrictionType.NONE ? "" : " - " + biomes.size());
    }
}
