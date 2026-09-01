package neresources.api.restriction;

import net.minecraft.StatCollector;
import net.minecraft.World;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The dimensions MITE ships with. Unlike Forge, MITE has a closed set of four, hard-coded in
 * {@code WorldProvider}, so there is no registry to query at runtime.
 */
public final class DimensionRegistry {
    private static final Map<Integer, String> NAME_KEYS;

    static {
        Map<Integer, String> keys = new LinkedHashMap<Integer, String>();
        keys.put(World.DIMENSION_ID_UNDERWORLD, "ner.dim.underworld");
        keys.put(World.DIMENSION_ID_NETHER, "ner.dim.nether");
        keys.put(World.DIMENSION_ID_OVERWORLD, "ner.dim.overworld");
        keys.put(World.DIMENSION_ID_THE_END, "ner.dim.end");
        NAME_KEYS = Collections.unmodifiableMap(keys);
    }

    private DimensionRegistry() {
    }

    /** @return every dimension id, in world order from the underworld up to the end. */
    public static Iterable<Integer> getDimensions() {
        return NAME_KEYS.keySet();
    }

    /** @return a localised dimension name, or the bare id if the dimension is unknown. */
    public static String getDimensionName(int dimensionId) {
        String key = NAME_KEYS.get(dimensionId);
        return key == null ? String.valueOf(dimensionId) : StatCollector.translateToLocal(key);
    }
}
