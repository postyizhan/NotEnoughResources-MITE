package emiresources.entry;

import emiresources.api.distribution.DistributionBase;
import emiresources.api.distribution.DistributionHelpers;
import emiresources.api.restriction.Restriction;
import emiresources.util.MapKeys;
import emiresources.util.StackHelper;
import net.minecraft.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One line on the ore graph: the ores that share it, their combined height distribution, and what
 * mining them yields.
 * <p>
 * Ores are grouped rather than listed one per graph so that, for example, a mod registering a second
 * distribution for the same ore adds to the existing curve instead of producing a duplicate page.
 * Grouping only happens when the restrictions describe the same places, so the overworld and
 * underworld variants of an ore stay on separate graphs — their curves have nothing in common.
 */
public class OreMatchEntry {
    private final Restriction restriction;
    private final List<OreSource> sources = new ArrayList<OreSource>();
    private final List<ItemStack> drops = new ArrayList<ItemStack>();
    private final Map<String, Boolean> silkTouchByItem = new LinkedHashMap<String, Boolean>();

    private float[] chances = new float[DistributionHelpers.HEIGHT];
    private int minY;
    private int maxY;
    private int bestY;
    private final int colour;

    public OreMatchEntry(ItemStack ore, DistributionBase distribution, Restriction restriction,
                         boolean needsSilkTouch, int colour) {
        this.restriction = restriction;
        this.colour = colour;
        add(ore, distribution, needsSilkTouch);
    }

    /**
     * Adds another ore to this graph.
     *
     * @return false if the ore belongs somewhere else, leaving this entry untouched
     */
    public boolean add(ItemStack ore, DistributionBase distribution, Restriction other, boolean needsSilkTouch) {
        if (!restriction.isMergeable(other)) {
            return false;
        }
        add(ore, distribution, needsSilkTouch);
        return true;
    }

    private void add(ItemStack ore, DistributionBase distribution, boolean needsSilkTouch) {
        sources.add(new OreSource(ore, distribution));
        silkTouchByItem.put(MapKeys.key(ore), needsSilkTouch);
        recalculate();
    }

    private void recalculate() {
        chances = new float[DistributionHelpers.HEIGHT];
        for (OreSource source : sources) {
            DistributionHelpers.addDistribution(chances, source.distribution.getDistribution());
        }

        int first = -1;
        int last = -1;
        for (int y = 0; y < chances.length; y++) {
            if (chances[y] > 0.0F) {
                if (first < 0) {
                    first = y;
                }
                last = y;
            }
        }
        minY = first < 0 ? 0 : first;
        maxY = last < 0 ? DistributionHelpers.HEIGHT - 1 : last;
        bestY = sources.size() == 1
                ? sources.get(0).distribution.getBestHeight()
                : DistributionHelpers.calculateMeanLevel(chances);
    }

    /** @return the per-Y chances over the whole world height. */
    public float[] getChances() {
        return chances;
    }

    /**
     * @param extraRange how many extra levels to show beyond the ore's range, for context
     * @return the slice of the curve worth drawing
     */
    public float[] getGraphSlice(int extraRange) {
        return Arrays.copyOfRange(chances, getGraphStartY(extraRange), getGraphEndY(extraRange) + 1);
    }

    /** @return the Y the graph slice starts at, so hover positions can be mapped back to a height. */
    public int getGraphStartY(int extraRange) {
        return Math.max(minY - extraRange, 0);
    }

    /** @return the Y the graph slice ends at, inclusive. */
    public int getGraphEndY(int extraRange) {
        return Math.min(maxY + extraRange, DistributionHelpers.HEIGHT - 1);
    }

    public int getBestY() {
        return bestY;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getColour() {
        return colour;
    }

    public Restriction getRestriction() {
        return restriction;
    }

    /** @return true if the item only drops when mined with Silk Touch. */
    public boolean isSilkTouchNeeded(ItemStack stack) {
        Boolean needed = silkTouchByItem.get(MapKeys.key(stack));
        return needed != null && needed;
    }

    /** Registers something the ore yields when mined, which is not the ore block itself. */
    public void addDrop(ItemStack drop, boolean needsSilkTouch) {
        for (ItemStack existing : drops) {
            if (StackHelper.isSameItem(existing, drop)) {
                return;
            }
        }
        drops.add(drop);
        silkTouchByItem.put(MapKeys.key(drop), needsSilkTouch);
    }

    public List<ItemStack> getDrops() {
        return drops;
    }

    /** @return the ore blocks and their drops, which is what the recipe cycles through. */
    public List<ItemStack> getOresAndDrops() {
        Set<String> seen = new LinkedHashSet<String>();
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (OreSource source : sources) {
            if (seen.add(MapKeys.key(source.ore))) {
                result.add(source.ore);
            }
        }
        for (ItemStack drop : drops) {
            if (seen.add(MapKeys.key(drop))) {
                result.add(drop);
            }
        }
        return result;
    }

    /** @return true if the stack is either one of the ores or one of their drops. */
    public boolean matches(ItemStack stack) {
        for (ItemStack candidate : getOresAndDrops()) {
            if (StackHelper.isSameItem(candidate, stack)) {
                return true;
            }
        }
        return false;
    }

    /** @return the first ore on this graph, used for naming and sorting. */
    public ItemStack getPrimaryOre() {
        return sources.get(0).ore;
    }

    public List<String> getRestrictionLines() {
        return restriction.getStringList();
    }

    @Override
    public String toString() {
        return StackHelper.nameOf(getPrimaryOre()) + " - " + restriction;
    }

    private static final class OreSource {
        private final ItemStack ore;
        private final DistributionBase distribution;

        OreSource(ItemStack ore, DistributionBase distribution) {
            this.ore = ore;
            this.distribution = distribution;
        }
    }
}
