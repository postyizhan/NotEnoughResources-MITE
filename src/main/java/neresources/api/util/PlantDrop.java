package neresources.api.util;

import net.minecraft.ItemStack;

/**
 * One entry in a plant's drop table. Plants express their drops in three different ways depending on
 * the block, so the kind decides how the number next to the item is rendered.
 */
public class PlantDrop {
    public enum DropKind {
        /** A flat chance to drop one item. */
        CHANCE,
        /** A weight relative to the other drops of the same plant. */
        WEIGHT,
        /** A quantity range that always drops. */
        MIN_MAX
    }

    private final ItemStack drop;
    private final DropKind dropKind;
    private final int itemWeight;
    private final int minDrop;
    private final int maxDrop;
    private final float chance;

    /** A drop picked out of a weighted pool, such as the seeds from tall grass. */
    public PlantDrop(ItemStack drop, int itemWeight) {
        this.drop = drop;
        this.itemWeight = itemWeight;
        this.dropKind = DropKind.WEIGHT;
        this.minDrop = 1;
        this.maxDrop = 1;
        this.chance = 1.0F;
    }

    /** A drop with a flat chance of appearing. */
    public PlantDrop(ItemStack drop, float chance) {
        this.drop = drop;
        this.chance = chance;
        this.dropKind = DropKind.CHANCE;
        this.itemWeight = 0;
        this.minDrop = 1;
        this.maxDrop = 1;
    }

    /** A drop that always appears, in a quantity range. */
    public PlantDrop(ItemStack drop, int minDrop, int maxDrop) {
        this.drop = drop;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
        this.dropKind = DropKind.MIN_MAX;
        this.itemWeight = 0;
        this.chance = 1.0F;
    }

    public ItemStack getDrop() {
        return drop;
    }

    public DropKind getDropKind() {
        return dropKind;
    }

    public int getWeight() {
        return itemWeight;
    }

    public int getMinDrop() {
        return minDrop;
    }

    public int getMaxDrop() {
        return maxDrop;
    }

    public float getChance() {
        return chance;
    }

    /**
     * @param totalWeight the plant's total drop weight, used only for {@link DropKind#WEIGHT}
     * @return the label shown next to the item
     */
    public String describe(int totalWeight) {
        switch (dropKind) {
            case WEIGHT:
                if (totalWeight <= 0) {
                    return "";
                }
                return formatPercent(100.0F * itemWeight / totalWeight);
            case CHANCE:
                return formatPercent(chance * 100.0F);
            default:
                return minDrop == maxDrop ? String.valueOf(minDrop) : minDrop + "-" + maxDrop;
        }
    }

    private static String formatPercent(float percent) {
        String formatted = percent < 10.0F
                ? String.format("%.1f", percent)
                : String.format("%d", (int) percent);
        return formatted.replace(',', '.') + "%";
    }
}
